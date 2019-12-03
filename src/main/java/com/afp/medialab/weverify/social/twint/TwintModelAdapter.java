package com.afp.medialab.weverify.social.twint;

import com.afp.medialab.weverify.social.model.twint.StopWords;
import com.afp.medialab.weverify.social.model.twint.TwintModel;
import com.afp.medialab.weverify.social.model.twint.TwittieDeserializer;
import com.afp.medialab.weverify.social.model.twint.TwittieResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Configuration
public class TwintModelAdapter {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwintThread.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${application.twittie.url}")
    private String twitieURL;

    private Map<String, List<String>> stopwords;
    private List<String> regExps;

    private String tweet;

    @PostConstruct
    private void stopWordsFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        stopwords = mapper.readValue(new File("src/main/resources/stopwords.json"), Map.class);

    }

    @PostConstruct
    private void regExps() throws IOException {
        FileReader fr = new FileReader("src/main/resources/regexp.txt");
        regExps = new ArrayList<>();
        BufferedReader br = new BufferedReader(fr);
        String str;
        while((str = br.readLine())!= null)
            regExps.add(str);

    }

    private String getTweetLang(String[] langs) {
        Map<String, Float> percents = new TreeMap<>();

        Arrays.stream(langs).forEach(lang -> {
            percents.put(lang, 0f);
            for (String stopword : stopwords.get(lang)) {
                String word = stopword;
                if (tweet.toLowerCase().contains(" " + word + " "))
                    percents.put(lang, percents.get(lang) + 1);
            }
            percents.put(lang, percents.get(lang) / (stopwords.get(lang)).size());
        });

        return percents.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

    }

    private Map<String, String> callTwittie() throws IOException {

        //HTTPConnexion Timeout
        ResponseEntity<String> response = null;
        try {
           response = restTemplate.postForEntity("http://185.249.140.38/weverify-twitie/process?annotations=:Person,:UserID,:Location,:Organization", tweet, String.class);

        }
        catch (Exception e){
            Logger.error("FAILED CALLING TWITTIE" );
            return null;
        }
        Logger.info("SUCCESSFULLY CALLED TWITTIE");
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(response.getBody());
        JsonNode annotations = root.get("response").get("annotations");

        TwittieResponse twittieResponse = mapper.treeToValue(annotations, TwittieResponse.class);
        Map<String, String> tokenJSON = new HashMap<>();

        twittieResponse.getPerson().forEach(p -> {
            String per = p.getFeatures().getString();
            String n_per = per.toLowerCase().replaceAll(" |/.", "_");
            tweet = tweet.replaceAll(per, n_per);

            tokenJSON.put(n_per, "Person");
        });
        twittieResponse.getUserID().forEach(p -> {
            tweet = tweet.replaceAll(p.getFeatures().getString(), p.getFeatures().getString().toLowerCase().replaceAll("@", ""));
            tokenJSON.put("@" + p.getFeatures().getString().toLowerCase(), "UserID");
        });
        twittieResponse.getLocation().forEach(p -> {
            String per = p.getFeatures().getString();
            String n_per = per.toLowerCase().replaceAll(" |/.", "_");
            tweet = tweet.replaceAll(per, n_per);
            tokenJSON.put(n_per, "Location");
        });
        twittieResponse.getOrganization().forEach(p -> {
            String per = p.getFeatures().getString();
            String n_per = per.toLowerCase().replaceAll(" |/.", "_");
            tweet = tweet.replaceAll(per, n_per);
            tokenJSON.put(n_per, "Organization");
        });

        return tokenJSON;
    }

    public void buildWit(TwintModel tm) throws InterruptedException, ParseException, IOException {

        tweet = tm.getTweet();
        Map<String, String> tokensNamed = callTwittie();

        String[] langs = new String[]{"fr", "en"};

        List<String> stopLang = stopwords.get(getTweetLang(langs));
        List<String> stopGlob = stopwords.get("glob");

        stopGlob.addAll(Arrays.asList(tm.getSearch().split(" ")));
        //String tweet = tm.getTweet();

        for (String regExp : regExps) {
            tweet = tweet.replaceAll(regExp, " ");
        }
           List<String> words = Arrays.asList(tweet.toLowerCase().split(" "));

            Map<String, Integer> occurences = new HashMap<>();

            words.stream().forEach((word) -> {

                if (!stopLang.contains(word) && !stopGlob.contains(word))
                    if (occurences.get(word) == null)
                        occurences.put(word, 1);
                    else
                        occurences.put(word, occurences.get(word) + 1);
            });

            List<TwintModel.WordsInTweet> wit = new ArrayList<>();

            occurences.forEach((word, occ) -> {
                TwintModel.WordsInTweet w = new TwintModel.WordsInTweet();
                w.setWord(word);
                w.setNbOccurences(occ);
                w.setEntity((tokensNamed != null && tokensNamed.get(word) != null)? tokensNamed.get(word) : null);

                wit.add(w);
            });

            tm.setTwittieTweet(tweet);
            tm.setWit(wit);
    }

    @Bean
    public RestTemplate restTemplate()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(5_000);

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(5_000);
        return new RestTemplate(clientHttpRequestFactory);
    }

    public String getTweet() {
        return tweet;
    }
}
