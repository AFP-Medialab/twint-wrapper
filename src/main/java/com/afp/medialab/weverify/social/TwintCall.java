package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class TwintCall {

   @Autowired
   private CollectService collectService;

   private static Logger Logger = LoggerFactory.getLogger(TwintCall.class);

   public Status collect(CollectRequest request, String name)
   {
      collectService.SaveCollectInfo(name, request, null, null, Status.NotStarted);
      collectService.UpdateCollectStatus(name, Status.Running);

      try {
         SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
         String fromStr = format.format(request.getFrom());
         String untilStr = format.format(request.getUntil());

         Thread process = new Thread() {
            public void run() {
               ProcessBuilder pb =
                       new ProcessBuilder("/bin/bash", "-c",
                               "docker run --rm --network twint_esnet -i medialab.registry.afp.com/twint:2.1.1 \"twint -s '" + request.getSearch() +
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

                  while ((s = stdInput.readLine()) != null) {
                     Logger.info(s);
                  }

                  stdInput.close();
                  stdError.close();

               } catch (IOException e) {
                  e.printStackTrace();
                  collectService.UpdateCollectStatus(name,Status.Error);
               }
            }
         };
            // read any errors from the attempted command

         process.start();
         collectService.UpdateCollectStatus(name,Status.Done);

         return Status.Done;

         } catch (Exception e) {
         Logger.error(e.getMessage());
         collectService.UpdateCollectStatus(name,Status.Error);
         e.printStackTrace();
         return Status.Error;
      }
   }
 /* */
}
