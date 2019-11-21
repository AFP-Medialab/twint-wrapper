package com.afp.medialab.weverify.social.twint;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.afp.medialab.weverify.social.model.*;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
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
	private ESOperations esOperation;

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
	public CompletableFuture<Integer> callTwint(Object request, String session, Integer cpt) throws IOException {

		Logger.debug("Started Thread n°" + cpt);
		Integer result = -1;
		if (request instanceof CollectRequest) {
			result = callProcessUntilSuccess((CollectRequest) request, session);
		}
		if (request instanceof CollectFollowsRequest)
			result = callFollowProcessUntilSuccess((CollectFollowsRequest)request, session);

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

	private ProcessBuilder createProcessBuilderFollows(CollectFollowsRequest request, String session, String type){
		boolean isDocker = isDockerCommand(twintCall);
		String twintRequest = TwintRequestGenerator.getInstance().generateFollowRequest(request.getUser(), session, type, isDocker, esURL);

		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", twintCall + twintRequest);
		processBuilder.environment().put("PATH", "/usr/bin:/usr/local/bin:/bin");
		Logger.info(twintCall + twintRequest);
		return processBuilder;

	}
	private ProcessBuilder createProcessBuilder(CollectRequest request, String session) {
		boolean isDocker = isDockerCommand(twintCall);

		String twintRequest = TwintRequestGenerator.getInstance().generateRequest(request, session, isDocker, esURL);


		ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", twintCall + twintRequest);
		processBuilder.environment().put("PATH", "/usr/bin:/usr/local/bin:/bin");
		Logger.info(twintCall + twintRequest);
		return processBuilder;
	}

	private Integer callProcess(ProcessBuilder processBuilder, String got) throws IOException {
		Process process = processBuilder.start();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String LoggerString;

		Boolean error_occurred = false;

		while ((LoggerString = stdError.readLine()) != null) {

			Logger.error(LoggerString);
			//error_occurred = true;
		}

		Integer nb_tweets = -1;
		while ((LoggerString = stdInput.readLine()) != null) {

			Logger.info("WAITING");
			if (LoggerString.contains("Successfully collected")) {
				String str = LoggerString.split("Successfully collected ")[1].split(" ")[0];
				if (str.equals("all"))
					str = LoggerString.split("Successfully collected ")[1].split(" ")[1];
				nb_tweets = Integer.parseInt(str);
				Logger.info("Successfully collected: " + nb_tweets + " " + got);


			}
		}
		stdInput.close();
		stdError.close();
		if (error_occurred)
			return -1;
		return nb_tweets;
	}

	private Integer callTwintFollowsProcess(CollectFollowsRequest request, String session){
		Integer result = -1;
		try {
			ProcessBuilder processBuilderFollowers = createProcessBuilderFollows(request, session, "followers");
		//	ProcessBuilder processBuilderFollowing = createProcessBuilderFollows(request, session, "following");
			result = callProcess(processBuilderFollowers, "users");// + callProcess(processBuilderFollowing);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;

	}

	private Integer callFollowProcessUntilSuccess(CollectFollowsRequest request, String session) {
		// could add a request subdivision on error
		Integer nb_tweets = -1;
		for (int i = 0; i < restart_time && nb_tweets == -1; i++) {
			nb_tweets = callTwintFollowsProcess(request, session);
			if (nb_tweets == -1) {
				Logger.info("Error reprocessing ");
			}
		}
		return nb_tweets;
	}


	private Integer callTwintProcess(CollectRequest request, String session) {

		Integer result = -1;
		ProcessBuilder processBuilder = createProcessBuilder(request, session);
		try {

			Logger.info("Call Process");

			result = callProcess(processBuilder, "tweets");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Integer callProcessUntilSuccess(CollectRequest request, String session) throws IOException {
		// could add a request subdivision on error
		Integer nb_tweets = -1;
		for (int i = 0; i < restart_time && nb_tweets == -1; i++) {
			Logger.info("Call Process Until success");

			nb_tweets = callTwintProcess(request, session);
			if (nb_tweets == -1) {
				Logger.info("Error reprocessing ");
				Date collected_to = esOperation.findWhereIndexingStopped(request, session);
				if (collected_to != null) {
					request.setUntil(collected_to);
				}
			}
		}

		Logger.info("Nb tweets: " + nb_tweets);


		return nb_tweets;
	}



}