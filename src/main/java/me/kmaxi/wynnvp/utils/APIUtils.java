package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIUtils {

    private static final int createdUserResponseCode = 201;

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


    /**
     * Can be used both to register a new user based on their Discord account,
     * or to update roles of an existing user with the bot.
     * The system checks whether a user with the specified discordId exists, if not, it checks against discordName.
     * If both queries return nothing, a new user is registered with the provided displayName and discordId
     *
     * @param urlParameters Post Parameters to use. If an empty string is provided it will do nothing.
     * @return If a new account was created it returns this Users password, if no user was made it returns an empty string
     * @throws IOException If an error was encountered
     */
    public static String updateUserDataOnWebsite(String urlParameters) throws IOException {

        if (urlParameters.equals(""))
            return "";

        urlParameters = addActionAndAPIKey(urlParameters);

        HttpURLConnection conn = sendPostRequest(Config.URL_DiscordIntegration, urlParameters);

        System.out.println("Got response code: " + conn.getResponseCode() + ". Response message: " + conn.getResponseMessage());


        if (conn.getResponseCode() == createdUserResponseCode) {

            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            System.out.println("Result: " + result);
            return result.toString();
        }

        return "";
    }

    /**
     * Sends a URL Post request
     * @param requestURL The url to send the post request to
     * @param urlParameters The parameters to post to the url
     * @return The http connection where you can get response code, response message and other things
     * @throws IOException If an error was encountered
     */
    private static HttpURLConnection sendPostRequest(String requestURL, String urlParameters) throws IOException {
        //Post Request
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }

        return conn;
    }


    private static String addActionAndAPIKey(String urlParameters) {
        urlParameters += "&action=" + "syncUser";

        //We print before adding apikey to make it easily copyable
        System.out.println("Post parameters: " + urlParameters);

        urlParameters += "&apiKey=" + APIKeys.discordIntegrationAPIKey;
        urlParameters = urlParameters.substring(1);
        return urlParameters;
    }


}
