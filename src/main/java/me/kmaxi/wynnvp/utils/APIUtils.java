package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIUtils {

    public static JSONArray getJsonData(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return new JSONArray(result.toString());
    }


    public static void updateUserDataOnWebsite(String urlParameters) throws IOException {

        if (urlParameters.equals(""))
            return;

        //append = append.replace(" ", "%20");

        urlParameters += "&apiKey=" + APIKeys.discordIntegrationAPIKey;
        urlParameters += "&action=" + "syncUser";
        urlParameters = urlParameters.substring(1);

        System.out.println("Post parameters: " + urlParameters);



        //Post Request
        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int    postDataLength = postData.length;
        String request        = Config.URL_DiscordIntegration;
        URL    url            = new URL( request );
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        conn.setUseCaches( false );
        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
            wr.write( postData );
        }
        System.out.println("Response code: " + conn.getResponseCode());
    }

}
