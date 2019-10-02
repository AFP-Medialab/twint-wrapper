package com.afp.medialab.weverify.social.twint;

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

    @Value("${command.twint}")
    private String twintCall;

    @Autowired
    CollectService collectService;

    @Autowired
    private TwintThread tt;


    public Date addDuration(Date date, Duration duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendar.MINUTE, toIntExact(duration.toMinutes()));
        return calendar.getTime();
    }


    public ArrayList<CollectRequest> createListOfCollectRequest(CollectRequest request, Duration duration) {

        ArrayList<CollectRequest> collectRequestList = new ArrayList<CollectRequest>();
        Date new_from_date = request.getFrom();
        Date final_until = request.getUntil();
        Date new_until_date = addDuration(new_from_date, duration);

        /* while from < until */
        while (new_until_date.compareTo(final_until) < 0) {
            CollectRequest new_collectRequest = new CollectRequest(request);
            new_collectRequest.setFrom(new_from_date);
            new_collectRequest.setUntil(new_until_date);
            collectRequestList.add(new_collectRequest);
            new_from_date = new_until_date;
            new_until_date = addDuration(new_from_date, duration);
        }
        /* if stopped early add the last period missing*/
        if (new_from_date.compareTo(final_until) < 0) {
            CollectRequest new_collectRequest = new CollectRequest(request);
            new_collectRequest.setFrom(new_from_date);
            new_collectRequest.setUntil(final_until);
            collectRequestList.add(new_collectRequest);
        }
        return collectRequestList;
    }


    public ArrayList<CompletableFuture<Integer>> callTwintMultiThreaded(CollectRequest request, String session) {

        Duration duration = Duration.ZERO.plusMinutes(minutes_duration);
        ArrayList<CollectRequest> collectRequestList = createListOfCollectRequest(request, duration);
        collectService.updateCollectTotal_threads(session, collectRequestList.size());

        ArrayList<CompletableFuture<Integer>> result = new ArrayList<CompletableFuture<Integer>>();

        collectService.updateCollectStatus(session, Status.Running);
        Integer cpt = 0;
        for (CollectRequest collectRequest : collectRequestList) {
            result.add(tt.callTwint(collectRequest, session, cpt));
            cpt++;
        }
        return result;
    }
}
