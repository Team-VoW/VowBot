package me.kmaxi.wynnvp.linereport;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class LineReportManager {


    public static Guild guild = null;

    public static void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (guild == null) return;
                sendAllReports(guild);
            }
        }, 0, 10 * 1000);
    }


    public static void lineReportReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        if (!event.retrieveMessage().complete().getAuthor().getId().equals("821397022250369054")) return;

        String[] messageSplitByLine = message.split("\n");

        if (messageSplitByLine.length <= 3) return;

        String str = messageSplitByLine[3];
        String line = str.substring(str.indexOf(" ") + 1);
        line = line.replace("`", "");

        String yOrN = "none";

        switch (event.getReactionEmote().getAsCodepoints()) {
            case "U+2705":
                yOrN = "y";
                break;
            case "U+274c":
                yOrN = "n";
                break;
            case "U+1f399":
                yOrN = "v";
                break;
        }
        if (yOrN.equals("none")) return;

        sendLineAndDeleteMessage(line, yOrN, event.retrieveMessage().complete(), guild);


    }


    public static void sendAllReports(Guild guild) {
        try {
            JSONArray jsonArray = getJsonData("http://voicesofwynn.com/api/unvoiced-line-report/index?apiKey=" + Config.readingApiKey);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                AcceptFullVoicedDialogue.queueCheckIfDialogueWasSent(guild, jsonObject);


                String message = "\uD83E\uDDD1\u200D\uD83C\uDF3E `" + jsonObject.getString("NPC") + "`\n"
                        + "\uD83D\uDDFA `" + jsonObject.getInt("X") + "|" + jsonObject.getInt("Y") + "|" + jsonObject.getInt("Z") + "`\n"
                        + "\uD83D\uDCE3 `" + jsonObject.getString("reporter") + "`\n"
                        + "> `" + jsonObject.getString("message") + "`";


                guild.getTextChannelById(Config.reportedLines).sendMessage(message).queue(message1 -> {
                    message1.addReaction(Config.acceptUnicode).queue();
                    message1.addReaction(Config.declineUnicode).queue();
                    message1.addReaction(Config.microphoneUnicode).queue();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendAllAcceptedReports(MessageChannel messageChannel) {
        try {
            JSONArray jsonArray = getJsonData("http://voicesofwynn.com/api/unvoiced-line-report/accepted?apiKey=" + Config.readingApiKey);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String message = "\uD83E\uDDD1\u200D\uD83C\uDF3E `" + jsonObject.getString("NPC") + "`\n"
                        + "\uD83D\uDDFA `" + jsonObject.getInt("X") + "|" + jsonObject.getInt("Y") + "|" + jsonObject.getInt("Z") + "`\n"
                        + "> `" + jsonObject.getString("message") + "`";


                messageChannel.sendMessage(message).queue(message1 -> {
                    message1.addReaction(Config.declineUnicode).queue();
                    message1.addReaction(Config.microphoneUnicode).queue();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static JSONArray getJsonData(String urlToRead) throws Exception {
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


    public static void sendLineAndDeleteMessage(String fullLine, String acceptedString, Message message, Guild guild) {

        int responseCode = 0;
        try {
            responseCode = declineOrAcceptLine(fullLine, acceptedString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseCode != 204) {
            //Failed
            guild.getTextChannelById(Config.staffBotChat).sendMessage("Line: ´" + fullLine + "´ with status **" + acceptedString + "** got response code **" + responseCode + "**").queue();
        } else {
            if (acceptedString.equals("y")) {
                guild.getTextChannelById(Config.acceptedLines).sendMessage(message).queue(message1 -> {
                    message1.addReaction(Config.declineUnicode).queue();
                    message1.addReaction(Config.microphoneUnicode).queue();
                });
            }

            message.delete().queue();

        }

    }

    private static int declineOrAcceptLine(String fullLine, String acceptedString) throws IOException {

        System.out.println("Line: " + fullLine + " has been marked as " + acceptedString);

        URL url = new URL("https://voicesofwynn.com/api/unvoiced-line-report/resolve");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String data = "line=" + fullLine + "&answer=" + acceptedString + "&apiKey=testing";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();

        return http.getResponseCode();


    }

}
