package com.afp.medialab.weverify.social.twint;

import static java.lang.Math.toIntExact;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

@Service
public class TwintThreadGroup {

    private static Logger Logger = LoggerFactory.getLogger(TwintThreadGroup.class);

    @Value("${application.twintcall.twint_request_maximum_days}")
    private Long days_limit;

    @Value("${application.twintcall.twint_big_request_subdivisions}")
    private Long subdivisions;

    @Value("${command.twint}")
    private String twintCall;

    @Autowired
    CollectService collectService;

    @Autowired
    private TwintThread tt;


    private Date addDuration(Date date, Duration duration) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, toIntExact(duration.toMinutes()));
        return calendar.getTime();
    }
    
    private  ArrayList<CollectRequest> createListOfCollectRequest(List<CollectRequest> collectRequests){
    	 ArrayList<CollectRequest> collectRequestList = new ArrayList<>();
    	 
    	 for(CollectRequest collectRequest: collectRequests) {
    		 ArrayList<CollectRequest> collectList = createListOfCollectRequest(collectRequest);
    		 collectRequestList.addAll(collectList);
    	 }
    	 
    	 return collectRequestList;
    }


    private ArrayList<CollectRequest> createListOfCollectRequest(CollectRequest collectRequest) {

        ArrayList<CollectRequest> collectRequestList = new ArrayList<>();

        Long maximum_duration = days_limit * 86400000;
        Long request_duration = collectRequest.getUntil().getTime() - collectRequest.getFrom().getTime();

        if (request_duration < maximum_duration) {
            collectRequestList.add(collectRequest);
            return collectRequestList;
        }

        Duration interval_size = Duration.ofSeconds(request_duration / subdivisions / 1000);
        Date new_from_date = collectRequest.getFrom();
        Date final_until = collectRequest.getUntil();
        Date new_until_date = addDuration(new_from_date, interval_size);

        /* while from < until */
        while (new_until_date.compareTo(final_until) < 0) {
            CollectRequest new_collectRequest = new CollectRequest(collectRequest);
            new_collectRequest.setFrom(new_from_date);
            new_collectRequest.setUntil(new_until_date);
            collectRequestList.add(new_collectRequest);
            new_from_date = new_until_date;
            new_until_date = addDuration(new_from_date, interval_size);
        }
        /* if stopped early replace the last date by final_until*/
        if (new_from_date.compareTo(final_until) < 0) {
            CollectRequest new_collectRequest = new CollectRequest((CollectRequest)collectRequestList.get(collectRequestList.size() - 1));
            new_collectRequest.setUntil(final_until);
            collectRequestList.set(collectRequestList.size() - 1, new_collectRequest);
        }
        return collectRequestList;
    }

    @Async(value ="twintCallGroupTaskExecutor")
    public void callTwintMultiThreaded(CollectHistory collectHistory, CollectRequest request) {

        ArrayList<CollectRequest> collectRequestList = createListOfCollectRequest(request);

        callTwintThreads(collectRequestList, collectHistory);
    }
    
    @Async(value ="twintCallGroupTaskExecutor")
    public void callTwintMultiThreaded(CollectHistory collectHistory, List<CollectRequest> collectRequest) {

        ArrayList<CollectRequest> collectRequestList = createListOfCollectRequest(collectRequest);
        callTwintThreads(collectRequestList, collectHistory);
        
    }
    
    private void callTwintThreads( ArrayList<CollectRequest> collectRequestList, CollectHistory collectHistory) {
    	collectHistory.setTotal_threads(collectHistory.getTotal_threads() + collectRequestList.size());
        collectHistory.setStatus(Status.Running);
        collectService.save_collectHistory(collectHistory);
        ArrayList<CompletableFuture<Integer>> result = new ArrayList<>();

        Logger.debug("launch thread group");
        for (CollectRequest collectRequest : collectRequestList) {
            result.add(tt.callTwint(collectHistory, collectRequest));
        }
        CompletableFuture.allOf(result.toArray(new CompletableFuture<?>[result.size()])).join();
        //getOnAllList(result);
    }
    

    private void getOnAllList(List<CompletableFuture<Integer>> list) {
            for (CompletableFuture<Integer> thread : list) {
                try {
                    Integer res = thread.get();
                    Logger.info("result : " + res);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

    }
}
