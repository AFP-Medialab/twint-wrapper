package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.CollectResponse;
import com.afp.medialab.weverify.social.model.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TwintCall {
   Status status;
   CollectRequest request;
   String name = "test";

   public TwintCall(String hashtag, Date since, Date until, String name)
   {
      this.status = Status.NotStarted;
      request = new CollectRequest(hashtag, since, until);
      this.name = name;
   }
   public TwintCall(CollectRequest request, String name)
   {
      this.status = Status.NotStarted;
      this.request = request;
      this.name = name;
   }

   public CollectResponse collect()
   {
      status = Status.Running;

      try {
         SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");
         String fromStr = format.format(request.getFrom());
         String untilStr = format.format(request.getUntil());

         ProcessBuilder pb =
                 new ProcessBuilder("/bin/bash", "-c", "docker run --rm --network twint_esnet -i medialab.registry.afp.com/twint:2.1.1 \"twint -s '#" + request.getSearch() +
                         "' --stats --since " + fromStr + " --until " + untilStr  +
                         " -l fr --essid sess-" + name + " -es elasticsearch:9200\"");
         System.out.println(pb.command());
         Process p = pb.start();
         BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));

         BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
         String s = "";
         System.out.println("Here is the standard error of the command (if any):\n");
         while ((s = stdError.readLine()) != null) {

            System.out.println(s);
         }

         System.out.println("**********\n");
         // read the output from the command
         System.out.println("Here is the standard output of the command:\n");
         while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
         }

         // read any errors from the attempted command

            status = Status.Done;

         stdInput.close();
         stdError.close();

         System.exit(0);
         } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }
 /* */
}
