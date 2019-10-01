package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.dao.entity.CollectHistory;
import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static java.lang.Math.toIntExact;

@Service
public class TwintThreadGroup {

    private static Logger Logger = LoggerFactory.getLogger(TwintThreadGroup.class);

    @Value("${application.twintcall.twint_thread_interval_minutes}")
    private Long minutes_duration;


    @Value("${src.profile.twint}")
    private String twintCall;

    @Autowired
    CollectService collectService;

    @Autowired
    private TwintThread tt;

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
            CompletableFuture<Integer> future = tt.callTwint(collectRequest, name);
            result.add(future);
            Logger.info("Finished adding future");
        }
        return result;
    }
}
