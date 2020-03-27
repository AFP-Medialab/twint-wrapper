package com.afp.medialab.weverify.social.twint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.afp.medialab.weverify.social.model.CollectRequest;

/**
 * Generate twint command with elasticsearch
 *
 * @author Medialab
 */
public class TwintRequestGenerator {

    private static Logger Logger = LoggerFactory.getLogger(TwintRequestGenerator.class);


    private static final TwintRequestGenerator INSTANCE = new TwintRequestGenerator();

    public static TwintRequestGenerator getInstance() {
        return INSTANCE;
    }

    public String generateSearch(CollectRequest collectRequest) {
        if (collectRequest == null)
            return null;
        StringBuilder sb = new StringBuilder("");

        if (collectRequest.getKeywordList() != null) {
            ArrayList<String> and = new ArrayList<String>(collectRequest.getKeywordList());
            sb.append(and.get(0));
            for (int i = 1; i < and.size(); i++) {
                sb.append(" AND " + and.get(i));
            }
        }

        if (collectRequest.getBannedWords() != null)
            for (String s : collectRequest.getBannedWords()) {
                sb.append(" -" + s);
            }
        return sb.toString();
    }

    public String generateRequest(CollectRequest cr, String id, boolean isDocker, String esURL) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean hasUser = false;
        String call = "twint -ho --count ";

        call += "-s '" + generateSearch(cr) + "'";

        if (cr.getUserList() != null && !cr.getUserList().isEmpty()) {
        	hasUser = true;
            String users = String.join(",", cr.getUserList());
            if (cr.getUserList().size() > 1)
                call += " --userlist '" + users + "'";
            else
                call += " -u " + users;
        }

        if (cr.getFrom() != null) {
            String fromStr = format.format(cr.getFrom());
            call += " --since '" + fromStr + "'";
        }

        if (cr.getUntil() != null) {
            String untilStr = format.format(cr.getUntil());

            call += " --until '" + untilStr + "'";
        }

        if (cr.getLang() != null && !cr.getLang().equals(""))
            call += " -l " + cr.getLang();

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

        if (cr.isVerified() && !hasUser)
            call += " --verified";
        //call += " --essid " + id + " -es " + esURL;
        call +=" -es " + esURL;
        if (isDocker)
            call = " \"" + call + "\"";
        Logger.debug("Twint command: " + call);
        return call;
    }

}
