package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.model.twint.TwintModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TwintModelAdapter {
    private static String getTweetLang(String text, String[] langs, JSONObject stopwords) {
        Map<String, Float> percents = new TreeMap<>();
        Arrays.stream(langs).forEach(lang -> {
            percents.put(lang, 0f);
            for (Object stopword : (JSONArray) stopwords.get(lang)) {
                String word = (String) stopword;
                if (text.contains(" " + word + " "))
                    percents.put(lang, percents.get(lang) + 1);
            }
            percents.put(lang, percents.get(lang) / ((JSONArray) stopwords.get(lang)).size());//Arrays.stream((Array) stopwords.get(lang))
        });
        return percents.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

    }

    private static Map<String, String> callTwittie(String tweet) throws IOException, InterruptedException, ParseException {
        URL url = new URL("http://185.249.140.38/weverify-twitie/process?annotations=:Person,:UserID,:Location,:Organization");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/plain");

        OutputStream os = con.getOutputStream();
        os.write(tweet.getBytes());
        os.flush();

        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
           // throw new RuntimeException("Failed : HTTP error code : "
             //       + con.getResponseCode());
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (con.getInputStream())));


        String output;
        String fullOut = "";
        while ((output = br.readLine()) != null) {
            fullOut += output;
        }
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(fullOut);

        JSONObject annotations = (JSONObject) ((JSONObject) json.get("response")).get("annotations");
        Map<String, String> tokenJSON = new HashMap<>();

        JSONArray orgaTwittieJSON = (JSONArray) annotations.get(":Organization");
        List<String> orgaJSON = new ArrayList<>();
        for (Object orga : orgaTwittieJSON)
        {
            String p = (String) ((JSONObject)((JSONObject) orga).get("features")).get("string");
            tokenJSON.put(p, "Organization");
        }

        JSONArray locTwittieJSON = (JSONArray) annotations.get(":Location");
        List<String> locJSON = new ArrayList<>();
        for (Object loc : locTwittieJSON)
        {
            String p = (String) ((JSONObject)((JSONObject) loc).get("features")).get("string");
            tokenJSON.put(p, "Location");
        }

        JSONArray personTwittieJSON = (JSONArray) annotations.get(":Person");
        List<String> personJSON = new ArrayList<>();
        for (Object person : personTwittieJSON)
        {
            String p = (String) ((JSONObject)((JSONObject) person).get("features")).get("string");
            tokenJSON.put(p, "Person");
        }

        JSONArray idsTwittieJSON = (JSONArray) annotations.get(":UserID");
        List<String> idsJSON = new ArrayList<>();
        for (Object id : idsTwittieJSON)
        {
            String p = (String) ((JSONObject)((JSONObject) id).get("features")).get("string");
            tokenJSON.put(p, "UserID");
        }

        con.disconnect();
        return tokenJSON;
    }

    public static void buildWit(TwintModel tm) throws InterruptedException, ParseException, IOException {
        Map<String, String> tokensNamed = callTwittie(tm.getTweet());
        JSONParser jp = new JSONParser();
        try (FileReader fr = new FileReader("src/main/resources/stopwords.json"))
        {
            Object obj =  jp.parse(fr);
            final JSONObject stopwords = (JSONObject) obj;

            String[] langs = new String[]{"fr", "en"};
            List<String> words = Arrays.asList(tm.getTweet().toLowerCase()
                    .replaceAll("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)|pic\\.twitter\\.com\\/([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)", "")
                    .replaceAll("[\\.\\(\\)0-9\\!\\?\\'\\’\\‘\\\"\\:\\,\\/\\\\\\%\\>\\<\\«\\»\\'\\#\\ \\;\\-\\&\\|]+|\\u00a9|\\u00ae|[\\u2000-\\u3300]|\\ud83c[\\ud000-\\udfff]|\\ud83d[\\ud000-\\udfff]|\\ud83e[\\ud000-\\udfff]", " ")
                    .split(" "));

            Map<String, Integer> occurences = new HashMap<>();

            words.stream().forEach((word) -> {
                JSONArray stopLang = (JSONArray) stopwords.get(getTweetLang(tm.getTweet(), langs, stopwords));
                JSONArray stopGlob = (JSONArray) stopwords.get("glob");

                if (!stopLang.contains(word) && !stopGlob.contains(word))
                    if (occurences.get(word) == null)
                        occurences.put(word, 1);
                    else
                        occurences.put(word, occurences.get(word)+1);
            });
           // word, occ,
           List<TwintModel.WordsInTweet> wit = new ArrayList<>();
            occurences.forEach((word, occ) -> {
                TwintModel.WordsInTweet w = new TwintModel.WordsInTweet();//word, occ, (tokensNamed != null)?tokensNamed.get(word):null);
                w.setWord(word); w.setNbOccurences(occ); w.setEntity((tokensNamed != null)?tokensNamed.get(word):null);
                wit.add(w);
            });


            tm.setWit(wit);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
