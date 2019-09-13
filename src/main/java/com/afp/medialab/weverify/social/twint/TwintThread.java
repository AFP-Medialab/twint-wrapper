package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.dao.repository.CollectInterface;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Configuration
public class TwintThread {

    private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);

    @Value("${src.profile.twint}")
    private String twintCall;


    @Autowired
    CollectService collectService;

    @Async
    @Transactional
    public CompletableFuture<Integer> callTwint(CollectRequest request, String name) {

        collectService.UpdateCollectStatus(name, Status.Running);
        CompletableFuture Result = CompletableFuture.completedFuture(-1);
        Status endStatus = Status.Done;
        String endMessage = "";
        try {

            String r = TwintRequestGenerator.generateRequest(request, name);
            ProcessBuilder pb =
                    new ProcessBuilder("/bin/bash", "-c", twintCall + r);

            pb.environment().put("PATH", "/usr/bin:/usr/local/bin:/bin");
            Logger.info(twintCall + r);
            Process p = null;
            try {
                p = pb.start();

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));

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
                    endStatus = Status.Error;
                    endMessage = "Error while collecting tweets";
                } else {
                    Logger.info("Updating status");
                    endMessage = "Collected " + nb_tweets.toString() + " successfully.";
                }

                stdInput.close();
                stdError.close();

                Result = CompletableFuture.completedFuture(nb_tweets);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
        }

        collectService.UpdateCollectStatus(name, endStatus);
        collectService.UpdateCollectMessage(name, endMessage);

        return Result;

    }
}
