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
        collectService.UpdateCollectStatus(name, Status.Running);

        Logger.info("STATUS RUNNING : " +  collectService.getCollectInfo(name).getStatus().toString());
        try {
            SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
            String fromStr = format.format(request.getFrom());
            String untilStr = format.format(request.getUntil());
                    ProcessBuilder pb =
                            new ProcessBuilder("/bin/bash", "-c",
                                            "PATH=/usr/bin:/usr/local/bin:/bin; docker run --rm --network twint_esnet -i medialab.registry.afp.com/twint:2.1.1 \"twint -s '" + request.getSearch() +
                                            "' --stats --since " + fromStr + " --until " + untilStr +
                                            " -l fr --essid sess-" + name + " -es elasticsearch:9200\"");

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

                        Logger.info("STATUS DONE : " + collectService.getCollectInfo(name).getStatus().toString());
                        stdInput.close();
                        stdError.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        collectService.UpdateCollectStatus(name, Status.Error);

                        Logger.info("STATUS ERROR : " + collectService.getCollectInfo(name).getStatus().toString());
                    }



        } catch (Exception e) {
            Logger.error(e.getMessage());
            collectService.UpdateCollectStatus(name, Status.Error);
            Logger.info("STATUS ERROR : ", collectService.getCollectInfo(name));
            e.printStackTrace();
        }

    }
}
