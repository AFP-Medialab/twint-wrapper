package com.afp.medialab.weverify.social.twint;

import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class myHttpUrlConnection {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(myHttpUrlConnection.class);
    private static HttpURLConnection con;

    public static String postRequest(String given_url, String json){

        String json_result = "";
        try {
            URL url = new URL(given_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000);
            connection.setRequestProperty("content-Type", MediaType.APPLICATION_JSON_VALUE);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            OutputStream request = connection.getOutputStream();
            request.write(json.getBytes("UTF-8"));
            request.close();

            InputStream response = new BufferedInputStream(connection.getInputStream());
            Scanner scanner = new Scanner(response, "UTF-8").useDelimiter("\\A");
            json_result = scanner.hasNext() ? scanner.next() : "";
        }
        catch (Exception e){
            Logger.error(e.toString());
        }
        Logger.debug(json_result);
        return json_result;
    }


    public String makeElasticSearchJsonQuery(Date since, Date until, String session){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String since_str = "";
        String until_str = "";
        try{
            since_str = dateFormat.format(since);
            until_str = dateFormat.format(until);
        }catch (Exception e ){
            Logger.error(e.toString());
        }

        String result = "{" +
                "        \"size\": 1," +
                "        \"sort\": [" +
                "                   {" +
                "                       \"date\": {" +
                "                           \"order\": \"asc\"" +
                "                       }" +
                "           }" +
                "       ]," +
                "        \"query\": {" +
                "            \"bool\": {" +
                "                \"must\": [" +
                "                    {" +
                "                        \"match_phrase\": {" +
                "                            \"essid\": {" +
                "                                \"query\": \"" + session + "\"" +
                "                            }" +
                "                        }" +
                "                    }," +
                "                    {" +
                "                        \"range\": {" +
                "                            \"date\": {" +
                "                                \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"," +
                "                                \"gte\": \"" + since_str + "\"," +
                "                                \"lte\": \"" + until_str + "\"" +
                "                            }" +
                "                        }" +
                "                    }" +
                "                ]" +
                "            }" +
                "        }" +
                "    }";

        return result;
    }
}
