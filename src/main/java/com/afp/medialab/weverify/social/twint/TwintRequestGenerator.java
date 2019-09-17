package com.afp.medialab.weverify.social.twint;
import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.SearchModel;

import java.text.SimpleDateFormat;

public class TwintRequestGenerator {
    public static String generateSearch(SearchModel search)
    {
        StringBuilder sb = new StringBuilder(search.getSearch());

        if (search.getAnd() != null)
            for (String s : search.getAnd()) {
                sb.append(" AND " + s);
            }

        if (search.getOr() != null)
            for (String s : search.getOr()) {
                sb.append(" OR " + s);
            }
        if (search.getNot() != null)
            for (String s : search.getNot()) {
                sb.append(" -" + s);
            }
        return sb.toString();
    }

    public static String generateRequest(CollectRequest cr, String id)
    {
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd");

        String call = " \"twint -ho --count ";

        if (cr.getSearch()!= null)
            call += "-s '" + generateSearch(cr.getSearch())  + "'";

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

        if (cr.getMedia() != null) {
            if (cr.getMedia().equals("both"))
                call += " --media";
            else if (cr.getMedia().equals("image"))
                call += " --images";
            else if (cr.getMedia().equals("video"))
                call += " --videos";
        }

        if (cr.getRetweetsHandling() != null) {
            if (cr.getRetweetsHandling().equals("exclude"))
                call += " -fr";
            if (cr.getRetweetsHandling().equals("only"))
                call += " -nr";
            if (cr.getRetweetsHandling().equals("allowed"))
                call += " --retweets";
        }

        if (cr.isVerified())
            call += " --verified";
        call += " --essid sess-" + id + " -es elasticsearch:9200\"";

        return call;
    }


}
