package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public static JSONObject getJsonObject(String urlToRead) throws Exception {
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
        return new JSONObject(result.toString());
    }




    /**
     * Can be used both to register a new user based on their Discord account,
     * or to update roles of an existing user with the bot.
     * The system checks whether a user with the specified discordId exists, if not, it checks against discordName.
     * If both queries return nothing, a new user is registered with the provided displayName and discordId
     * @param urlParameters Post Parameters to use. If an empty string is provided it will do nothing.
     * @return If a new account was created it returns this Users password, if no user was made it returns an empty string
     * @throws IOException If an error was encountered
     */
    public static String updateUserDataOnWebsite(String urlParameters) throws IOException {

        if (urlParameters.equals(""))
            return "";

        urlParameters = addActionAndAPIKey(urlParameters);

        HttpURLConnection conn = sendPostRequest(Config.URL_DiscordIntegration, urlParameters);

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

    /**
     * Sends a URL Post request
     * @param requestURL The url to send the post request to
     * @param urlParameters The parameters to post to the url
     * @return The http connection where you can get response code, response message and other things
     * @throws IOException If an error was encountered
     */
    public static HttpURLConnection sendPostRequest(String requestURL, String urlParameters) throws IOException {
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

    public static int sendPUT(String urlString, String data, String apiKey) throws IOException {


        URL url = new URL(urlString);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        data = data + "&apiKey=" + apiKey;

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();

        return http.getResponseCode();
    }


}
