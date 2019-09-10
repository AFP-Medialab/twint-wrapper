package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TwintCall {
   private CollectRequest request;
   private String name;

   @Autowired
   private CollectService collectService;

   private static Logger Logger = LoggerFactory.getLogger(TwintCall.class);

   public TwintCall(String hashtag, Date since, Date until, String name)
   {

      request = new CollectRequest(hashtag, since, until);

      collectService.SaveCollectInfo(name, request, null, null, Status.NotStarted);

      this.name = name;
   }
   public TwintCall(CollectRequest request, String name)
   {
      collectService.SaveCollectInfo(name, request, null, null, Status.NotStarted);

      this.request = request;
      this.name = name;
   }

   public Status collect()
   {
      collectService.UpdateCollectStatus(name, Status.Running);

      try {
         SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
         String fromStr = format.format(request.getFrom());
         String untilStr = format.format(request.getUntil());

         ProcessBuilder pb =
                 new ProcessBuilder("/bin/bash", "-c",
                         "docker run --rm --network twint_esnet -i medialab.registry.afp.com/twint:2.1.1 \"twint -s '" + request.getSearch() +
                         "' --stats --since " + fromStr + " --until " + untilStr  +
                         " -l fr --essid sess-" + name + " -es elasticsearch:9200\"");

         Logger.debug(pb.command().toString());

         Process p = pb.start();
         BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));

         BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
         String s = "";
        // Logger.info("Here is the standard error of the command (if any):\n");
         while ((s = stdError.readLine()) != null) {

            Logger.error(s);
         }

         // read the output from the command
        // Logger.info("Here is the standard output of the command:\n");
         while ((s = stdInput.readLine()) != null) {
            Logger.info(s);
         }

         // read any errors from the attempted command

         collectService.UpdateCollectStatus(name, Status.Done);

         stdInput.close();
         stdError.close();


         return Status.Done;

         } catch (Exception e) {
         Logger.error(e.getMessage());
         e.printStackTrace();
         return Status.Error;
      }
   }
 /* */
}
