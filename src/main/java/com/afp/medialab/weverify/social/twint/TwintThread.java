package com.afp.medialab.weverify.social.twint;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import static java.lang.Math.toIntExact;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;

@Service
public class TwintThread {

    @Value("${application.twintcall.twint_thread_interval_minutes}")
    private Long minutes_duration;

    private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);

    @Value("${src.profile.twint}")
    private String twintCall;

    @Autowired
    CollectService collectService;


    public ArrayList<CompletableFuture<Integer>> callTwint2(CollectRequest request1, CollectRequest request2, String id) {
        CollectHistory collectHistory = collectService.getCollectInfo(id);
        String firstRequest = collectHistory.getQuery();
        Duration thread_interval = Duration.ZERO.plusMinutes(minutes_duration);
        collectService.updateCollectStatus(id, Status.Running);
        ArrayList<CompletableFuture<Integer>> res = new ArrayList<>(callTwintMultiThreaded(request1, id, thread_interval));
        Logger.info("RES: first list has " + res.size() + "threads");

        if (request2 != null)
            res.addAll(callTwintMultiThreaded(request2, id, thread_interval));

        Logger.info("RES: Final list has " + res.size() + "threads");

        return res;
    }

    private Object lock = new Object();

    @Async
    public CompletableFuture<Integer> callTwint(CollectRequest request, String name) {

        Integer result = -1;
        String endMessage = "";

        synchronized (lock) {
            CollectHistory collectHistory = collectService.getCollectInfo(name);
            collectService.updateCollectTotal_threads(name, collectHistory.getTotal_threads() + 1);
            Logger.info("Thread finished " + collectHistory.getFinished_threads());
            Logger.info("Thread total " + collectHistory.getTotal_threads());
        }

        Logger.info("RES : " + result.toString());
        Logger.info("from : " + request.getFrom());
        Logger.info("until : " + request.getUntil());
        try {

            String r = TwintRequestGenerator.generateRequest(request, name);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", twintCall + r);

            Status endStatus;
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
                    synchronized (lock) {
                        collectService.updateCollectStatus(name, Status.Error);
                    }
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

        synchronized (lock) {
            CollectHistory collectHistory = collectService.getCollectInfo(name);
            int finished_thread = collectHistory.getFinished_threads() + 1;
            collectService.updateCollectFinished_threads(name, finished_thread);

            Integer old_count = collectHistory.getCount();
            if (old_count == null || old_count == -1)
                collectService.updateCollectCount(name, result);
            else
                collectService.updateCollectCount(name, result + old_count);
        }

        synchronized (lock){
            collectService.updateCollectMessage(name, endMessage);
            CollectHistory collectHistory = collectService.getCollectInfo(name);
            int finished_thread = collectHistory.getFinished_threads();
            int total_threads = collectHistory.getTotal_threads();
            if (finished_thread == total_threads)
                collectService.updateCollectStatus(name, Status.Done);
        }
        return CompletableFuture.completedFuture(result);
    }

    public Date addDuration(Date date, Duration duration){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar.MINUTE, toIntExact(duration.toMinutes()));
        return calendar.getTime();
    }


    public ArrayList<CollectRequest> createListOfCollecRequest(CollectRequest request, Duration duration)
    {
        ArrayList<CollectRequest> collectRequestList = new ArrayList<>();

        Date new_from_date = request.getFrom();
        Date final_until = request.getUntil();
        Date new_until_date = addDuration(new_from_date, duration);

        /* while from < until */
        while (new_until_date.compareTo(final_until) < 0){
            CollectRequest new_collectrequest = new CollectRequest(request);
            new_collectrequest.setFrom(new_from_date);
            new_collectrequest.setUntil(new_until_date);
            collectRequestList.add(new_collectrequest);
            new_from_date = new_until_date;
            new_until_date = addDuration(new_from_date, duration);
        }
        /* if stopped early add the last period missing*/
        if (new_from_date.compareTo(final_until) < 0){
            CollectRequest new_collectrequest = new CollectRequest(request);
            new_collectrequest.setFrom(new_from_date);
            new_collectrequest.setUntil(final_until);
            collectRequestList.add(new_collectrequest);
        }
        return collectRequestList;
    }


    public ArrayList<CompletableFuture<Integer>> callTwintMultiThreaded(CollectRequest request, String name, Duration duration) {
        ArrayList<CollectRequest> collectRequestList = new ArrayList(createListOfCollecRequest(request, duration));


        ArrayList<CompletableFuture<Integer>> result = new ArrayList<>();
        for (CollectRequest collectRequest : collectRequestList){
            CompletableFuture<Integer> future = callTwint(collectRequest, name);
            result.add(future);
            Logger.info("Finished adding future");
        }
        return result;
    }
}