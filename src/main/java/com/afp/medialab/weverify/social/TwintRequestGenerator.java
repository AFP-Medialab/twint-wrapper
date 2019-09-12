package com.afp.medialab.weverify.social;

import com.afp.medialab.weverify.social.model.CollectRequest;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;

public class TwintRequestGenerator {




    public static String generateRequest(CollectRequest cr, String id)
    {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");

        String call = " \"twint -ho --count ";

        if (cr.getSearch()!= null)
            call += "-s '" + cr.getSearch() + "'";

        if (cr.getUser() != null)
            call += " -u " + cr.getUser();

        if (cr.getFrom() != null) {
            String fromStr = format.format(cr.getFrom());
            call += " --since " + fromStr;

        }

        if (cr.getUntil() != null) {
            String untilStr = format.format(cr.getUntil());

            call += " --until " + untilStr;
        }
        call += " -l ";

        if (cr.getLang() != null)
            call += cr.getLang();
        else
            call += "fr";

        call += " --essid sess-" + id + " -es elasticsearch:9200\"";

        return call;
    }


}
