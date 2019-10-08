package com.afp.medialab.weverify.social.twint;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import com.afp.medialab.weverify.social.model.twint.TwintModel;

/**
 * Run twint command in a asynchronous thread
 *
 * @author Medialab
 */
@Service
public class TwintThread {

	private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);

	@Value("${command.twint}")
	private String twintCall;

	@Value("${application.elasticsearch.url}")
	private String esURL;

	@Value("${application.twintcall.twint_thread_nb_restart_on_error}")
	private Long restart_time;

	@Autowired
	private ElasticsearchOperations esOperation;

	@Autowired
	CollectService collectService;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Object lock = new Object();

	private boolean isDockerCommand(String twintCall) {
		if (twintCall.startsWith("docker"))
			return true;
		else
			return false;
	}

	@Async(value = "twintCallTaskExecutor")
	public CompletableFuture<Integer> callTwint(CollectRequest request, String session, Integer cpt) {

		Logger.debug("Started Thread n°" + cpt);

		Integer result = callProcessUntilSuccess(request, session);

		// update db to say this thread is finished
		synchronized (lock) {
			CollectHistory collectHistory = collectService.getCollectInfo(session);
			int finished_thread = collectHistory.getFinished_threads() + 1;
			collectService.updateCollectFinishedThreads(session, finished_thread);

			Integer old_count = collectHistory.getCount();
			if (old_count == null || old_count == -1)
				collectService.updateCollectCount(session, result);
			else
				collectService.updateCollectCount(session, result + old_count);
		}

		if (result != -1) {
			synchronized (lock) {
				CollectHistory collectHistory = collectService.getCollectInfo(session);
				int successful_threads = collectHistory.getSuccessful_threads() + 1;
				collectService.updateCollectSuccessfulThreads(session, successful_threads);
			}
		}
		synchronized (lock) {
			CollectHistory collectHistory = collectService.getCollectInfo(session);
			int finished_threads = collectHistory.getFinished_threads();
			int successful_threads = collectHistory.getSuccessful_threads();
			int total_threads = collectHistory.getTotal_threads();
			if (finished_threads == total_threads) {
				collectService.updateCollectStatus(session, Status.Done);
				if (successful_threads == finished_threads)
					collectService.updateCollectMessage(session, "Finished successfully");
				else
					collectService.updateCollectMessage(session, "Parts of this search could not be found");
			}
		}
		return CompletableFuture.completedFuture(result);
	}

	private ProcessBuilder createProcessBuilder(CollectRequest request, String session) {
		boolean isDocker = isDockerCommand(twintCall);
		String twintRequest = TwintRequestGenerator.getInstance().generateRequest(request, session, isDocker, esURL);

		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", twintCall + twintRequest);
		processBuilder.environment().put("PATH", "/usr/bin:/usr/local/bin:/bin");
		Logger.info(twintCall + twintRequest);
		return processBuilder;
	}

	private Integer callProcess(ProcessBuilder processBuilder) throws IOException {
		Process process = processBuilder.start();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String LoggerString;

		Boolean error_occurred = false;

		while ((LoggerString = stdError.readLine()) != null) {
			Logger.error(LoggerString);
			error_occurred = true;
		}

		Integer nb_tweets = -1;
		while ((LoggerString = stdInput.readLine()) != null) {

			if (LoggerString.contains("Successfully collected")) {
				String str = LoggerString.split("Successfully collected ")[1].split(" ")[0];
				nb_tweets = Integer.parseInt(str);
				Logger.info("Successfully collected: " + nb_tweets + " tweets");
			}
		}
		stdInput.close();
		stdError.close();
		if (error_occurred)
			return -1;
		return nb_tweets;
	}

	private Integer callTwintProcess(CollectRequest request, String session) {

		Integer result = -1;
		try {
			ProcessBuilder processBuilder = createProcessBuilder(request, session);
			try {

				result = callProcess(processBuilder);

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private Integer callProcessUntilSuccess(CollectRequest request, String session) {
		// could add a request subdivision on error
		Integer nb_tweets = -1;
		for (int i = 0; i < restart_time && nb_tweets == -1; i++) {
			nb_tweets = callTwintProcess(request, session);
			if (nb_tweets == -1) {
				Logger.info("Error reprocessing ");
				Date collected_to = findWhereIndexingStopped(request, session);
				if (collected_to != null)
					request.setUntil(collected_to);
			}
		}
		return nb_tweets;
	}

	/**
	 * Get the latest tweet extracted for this session. (Oldest tweet is always
	 * searched)
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	private Date findWhereIndexingStopped(CollectRequest request, String session) {

		String sinceStr = dateFormat.format(request.getFrom());
		String untilStr = dateFormat.format(request.getUntil());

		QueryBuilder builder = boolQuery().must(matchQuery("essid", session)).filter(
				rangeQuery("date").format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis").gte(sinceStr).lte(untilStr));
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(builder).build()
				.addSort(Sort.by("date").ascending()).setPageable(PageRequest.of(0, 1));
		final List<TwintModel> hits = esOperation.queryForList(searchQuery, TwintModel.class);

		if (hits.size() < 1) {
			Logger.error("No tweets found");
			return null;
		}
		TwintModel twintModel = hits.get(0);

		return twintModel.getDate();

	}

}