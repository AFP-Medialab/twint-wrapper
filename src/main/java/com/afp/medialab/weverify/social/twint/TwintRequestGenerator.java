package com.afp.medialab.weverify.social.twint;

import java.text.SimpleDateFormat;

import com.afp.medialab.weverify.social.model.CollectRequest;
import com.afp.medialab.weverify.social.model.SearchModel;

/**
 * Generate twint command with elasticsearch
 * 
 * @author Medialab
 *
 */
public class TwintRequestGenerator {

	private static final TwintRequestGenerator INSTANCE = new TwintRequestGenerator();

	public static TwintRequestGenerator getInstance() {
		return INSTANCE;
	}

	public String generateSearch(SearchModel search) {
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

	public  String generateRequest(CollectRequest cr, String id, boolean isDocker, String esURL) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String call = "twint -ho --count ";

		if (cr.getSearch() != null)
			call += "-s '" + generateSearch(cr.getSearch()) + "'";

		if (cr.getUser_list() != null) {
			String users = String.join(",", cr.getUser_list());
			if (cr.getUser_list().size() > 1)
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

		if (cr.getLang() != null)
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

		if (cr.isVerified())
			call += " --verified";
		call += " --essid " + id + " -es " + esURL;
		if (isDocker)
			call = " \"" + call + "\"";
		return call;
	}

}
