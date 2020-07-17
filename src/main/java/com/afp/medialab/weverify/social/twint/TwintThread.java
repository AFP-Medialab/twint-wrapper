package com.afp.medialab.weverify.social.twint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;

/**
 * Run twint command in a asynchronous thread
 *
 * @author Medialab
 */
@Component("twintThread")
@Transactional
public class TwintThread {

	private static final Logger Logger = LoggerFactory.getLogger(TwintThread.class);

	@Value("${command.twint}")
	private String twintCall;

	@Value("${application.elasticsearch.url}")
	private String esURL;

	@Value("${application.twintcall.twint_thread_nb_restart_on_error}")
	private Long restart_time;

	@Autowired
	private ESOperations esOperation;
	
	@Autowired
	private CollectService collectService;

	// private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd
	// HH:mm:ss");

	// private Object lock = new Object();

	private boolean isDockerCommand(String twintCall) {
		if (twintCall.startsWith("docker"))
			return true;
		else
			return false;
	}

	@Async(value = "twintCallTaskExecutor")
	//@Transactional
	public CompletableFuture<Integer> callTwint(CollectHistory collectHistory, CollectRequest request) {

		Integer result = null;
		try {
			result = callProcessUntilSuccess(request, collectHistory.getSession());
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("Error calling twint process", e);
		}
		collectHistory.setFinished_threads(collectHistory.getFinished_threads() + 1);
		Integer old_count = collectHistory.getCount();
		if (old_count == null || old_count == -1)
			collectHistory.setCount(result);
		else
			collectHistory.setCount(result + old_count);
		collectService.save_collectHistory(collectHistory);

		if (result != -1) {
			collectHistory.setSuccessful_threads(collectHistory.getSuccessful_threads() + 1);
			collectService.save_collectHistory(collectHistory);
		}
		
		return CompletableFuture.completedFuture(result);
	}


	private ProcessBuilder createProcessBuilder(CollectRequest request, String session) {
		boolean isDocker = isDockerCommand(twintCall);
		// String twintRequest =
		// TwintRequestGenerator.getInstance().generateRequest(request, session,
		// isDocker, esURL);
		String twintRequest = TwintPlusRequestBuilder.getInstance().generateRequest(request, session, isDocker, esURL);

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
		Integer nb_tweets = -1;
		while ((LoggerString = stdError.readLine()) != null) {

			if (LoggerString.contains("Tweets collected")) {
				String str = LoggerString.split("Tweets collected: ")[1].split(" ")[0];
				str = str.substring(0, str.length() - 2);
				nb_tweets = Integer.parseInt(str);
				Logger.info("Successfully collected: " + nb_tweets + " " + got);

			}
			// error_occurred = true;
		}

		while ((LoggerString = stdInput.readLine()) != null) {
			Logger.error(LoggerString);

		}
		stdInput.close();
		stdError.close();
		if (error_occurred)
			return -1;
		return nb_tweets;
	}

	private Integer callTwintProcess(CollectRequest request, String session) {

		Integer result = -1;
		ProcessBuilder processBuilder = createProcessBuilder(request, session);
		try {
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
				Date collected_to = esOperation.findWhereIndexingStopped(request);
				if (collected_to != null) {
					request.setUntil(collected_to);
				}
			}
		}

		Logger.info("Nb tweets: " + nb_tweets);

		return nb_tweets;
	}

}