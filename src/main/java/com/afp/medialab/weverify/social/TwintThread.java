package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Configuration
public class TwintThread{

    private static Logger Logger = LoggerFactory.getLogger(TwintThread.class);

    @Autowired
    CollectService collectService;
  /*  private CollectService collectService;

    private CollectRequest request;
    private String name;

    public String getName() {
        return name;
    }

    public CollectRequest getRequest() {
        return request;
    }

    public TwintThread()
    {
        this.request = request;
        name = id;
        collectService = cs;
    }
*/
    @Async
    @Transactional
    public void callTwint(CollectRequest request, String name) {
        Logger.info("STATUS RUNNING : " +  collectService.getCollectInfo(name).getStatus().toString());
        try {

            String r = TwintRequestGenerator.generateRequest(request, name);
                    ProcessBuilder pb =
                            new ProcessBuilder("/bin/bash", "-c",
                                            "PATH=/usr/bin:/usr/local/bin:/bin; " + r);

                    Logger.info(r);
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
                      /*  if (s != "") {
                            stdInput.close();
                            stdError.close();
                            collectService.UpdateCollectStatus(name, Status.Error);
                        }*/

                        while ((s = stdInput.readLine()) != null) {
                            Logger.info(s);
                        }

                        collectService.UpdateCollectStatus(name, Status.Done);

                        stdInput.close();
                        stdError.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        collectService.UpdateCollectStatus(name, Status.Error);

                    }



        } catch (Exception e) {
            Logger.error(e.getMessage());
            collectService.UpdateCollectStatus(name, Status.Error);
            e.printStackTrace();
        }

    }
}
