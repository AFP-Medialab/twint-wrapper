package com.afp.medialab.weverify.social.twint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;

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

	@Autowired
	CollectService collectService;

	@Async
	public CompletableFuture<Map.Entry<Integer, Integer>> callTwint2(CollectRequest request1, CollectRequest request2,
			String id) {
		CollectHistory collectHistory = collectService.getCollectInfo(id);
		String firstRequest = collectHistory.getQuery();
		// Integer old_count = collectHistory.getCount();

		collectService.updateCollectStatus(id, Status.Running);
		Integer res = callTwint(request1, id);
		Logger.info("RES : " + res.toString());
		Integer res2 = -1;
		if (request2 != null)
			res2 = callTwint(request2, id);

		// If query hasn't change, no extension to the search added
		if (firstRequest.equals(collectService.getCollectInfo(id).getQuery())) {
			collectService.updateCollectStatus(id, Status.Done);
		}

		return CompletableFuture.completedFuture(new AbstractMap.SimpleEntry<>(res, res2));
	}

	public Integer callTwint(CollectRequest request, String name) {

		Integer result = -1;
		String endMessage = "";

		Logger.debug("RES : " + result.toString());
		try {
			boolean isDocker = isDockerCommand(twintCall);
			String r = TwintRequestGenerator.getInstance().generateRequest(request, name, isDocker, esURL);
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", twintCall + r);

			// Status endStatus;
			pb.environment().put("PATH", "/usr/bin:/usr/local/bin:/bin");
			Logger.info(twintCall + r);
			Process p = null;
			try {
				p = pb.start();

				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

				BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				String s = "";

				while ((s = stdError.readLine()) != null) {
					Logger.error(s);
				}

				Integer nb_tweets = -1;
				while ((s = stdInput.readLine()) != null) {
					Logger.info(s);
					if (s.contains("Successfully collected")) {
						String str = s.split("Successfully collected ")[1].split(" ")[0];
						nb_tweets = Integer.parseInt(str);
					}
				}

				if (nb_tweets == -1) {

					collectService.updateCollectStatus(name, Status.Error);
					endMessage = "Error while collecting tweets";
				} else {
					endMessage = "Collected " + nb_tweets.toString() + " successfully.";
				}

				stdInput.close();
				stdError.close();

				result = nb_tweets;

			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}

		Integer old_count = collectService.getCollectInfo(name).getCount();
		if (old_count == null || old_count == -1)
			collectService.updateCollectCount(name, result);
		else
			collectService.updateCollectCount(name, result + old_count);

		collectService.updateCollectMessage(name, endMessage);
		return result;
	}

	private boolean isDockerCommand(String twintCall) {
		if (twintCall.startsWith("docker"))
			return true;
		else
			return false;
	}
}