package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.dao.service.CollectService;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

@Configuration
public class TwintCall {

   @Autowired
   private CollectService collectService;

   private static Logger Logger = LoggerFactory.getLogger(TwintCall.class);

   public Status collect(TwintThread tt) {

      collectService.SaveCollectInfo(tt.getName(), tt.getRequest(), null, null, Status.NotStarted);
      collectService.UpdateCollectStatus(tt.getName(), Status.Running);

      tt.run();
      collectService.UpdateCollectStatus(tt.getName(),Status.Done);
      return Status.Done;

   }
}
