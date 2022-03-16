package me.kmaxi.wynnvp;

import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LineReportManager {

    private final static String urlToRead = "http://voicesofwynn.com/api/unvoiced-line-report/index?apiKey=testing";

    public static void sendAllReports(Guild guild) {
        try {
            JSONArray jsonArray = getUnprocessedReports();

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.getString("status").equals("unprocessed")) continue;

                String message = "**NPC:**         `" + jsonObject.getString("NPC") + "`\n"
                        + "**Location:** `" + jsonObject.getString("X") + "|" + jsonObject.getString("Y") +  "|" + jsonObject.getString("Z") + "`\n"
                        + "**Line:**         `" + jsonObject.getString("message") + "`";


                guild.getTextChannelById(Config.reportedLines).sendMessage(message).queue(message1 -> {
                    message1.addReaction("\u2705").queue();
                    message1.addReaction("\u274C").queue();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static JSONArray getUnprocessedReports() throws Exception {
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

}
