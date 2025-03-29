package me.kmaxi.wynnvp.linereport;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static me.kmaxi.wynnvp.WynnVPBotMain.guild;
import static me.kmaxi.wynnvp.utils.APIUtils.getJsonData;

public class LineReportManager {


    public static void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (guild == null) return;
                sendAllReports();
            }
        }, 0, 10 * 1000);
    }


    public static void lineReportReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        //If reacted to a message that was not sent from this bot or is not admin
        if (!event.retrieveMessage().complete().getAuthor().getId().equals("821397022250369054")
                || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;

        String line;
        if (event.getChannel().getIdLong() == Config.staffBotChat) {
            line = message;
        } else {
            String[] messageSplitByLine = message.split("\n");

            if (messageSplitByLine.length <= 3) return;

            String str = messageSplitByLine[3];
            line = str.substring(str.indexOf(" ") + 1);
            line = line.replace("`", "");
        }


        String yOrN = switch (event.getReaction().getEmoji().asUnicode().getAsCodepoints()) {
            case "U+2705" -> "y";
            case "U+274c" -> "n";
            case "U+1f399" -> "v";
            case "U+1f5d1" -> "r";
            default -> "none";
        };

        if (yOrN.equals("none")) return;

        sendLineAndDeleteMessage(line, yOrN, event.retrieveMessage().complete(), guild);


    }


    public static void sendAllReports() {
        try {
            JSONArray jsonArray = getJsonData("http://voicesofwynn.com/api/unvoiced-line-report/index?apiKey=" + APIKeys.readingApiKey);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                AcceptFullVoicedDialogue.queueCheckIfDialogueWasSent(guild, jsonObject);


                String message = "\uD83E\uDDD1\u200D\uD83C\uDF3E `" + jsonObject.getString("NPC") + "`\n"
                        + "\uD83D\uDDFA `" + jsonObject.getInt("X") + "|" + jsonObject.getInt("Y") + "|" + jsonObject.getInt("Z") + "`\n"
                        + "\uD83D\uDCE3 `" + jsonObject.getString("reporter") + "`\n"
                        + "> `" + jsonObject.getString("message") + "`";


                guild.getTextChannelById(Config.reportedLines).sendMessage(message).queue(message1 -> {
                    message1.addReaction(Emoji.fromUnicode(Config.acceptUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.declineUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.microphoneUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.trashUnicode)).queue();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void sendLineAndDeleteMessage(String fullLine, String acceptedString, Message message, Guild guild) {

        int responseCode = 0;
        try {
            responseCode = declineOrAcceptLine(fullLine, acceptedString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseCode >= 400) {
            //Failed
            guild.getTextChannelById(Config.staffBotChat).sendMessage("Line: ´" + fullLine + "´ with status **" + acceptedString + "** got response code **" + responseCode + "**").queue();
        } else {
            if (acceptedString.equals("y")) {
                guild.getTextChannelById(Config.acceptedLines).sendMessage(message.getContentRaw()).queue(message1 -> {
                    message1.addReaction(Emoji.fromUnicode(Config.declineUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.microphoneUnicode)).queue();
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

        String data = "line=" + fullLine + "&answer=" + acceptedString + "&apiKey=" + APIKeys.updateApiKey;

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();

        return http.getResponseCode();
    }

    public static void sendLinesWithReaction(String url, MessageChannelUnion messageChannel) {

        try {
            JSONArray jsonArray = getJsonData(url);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String line = jsonObject.getString("message");

                messageChannel.sendMessage(line).queue(message1 -> {
                    message1.addReaction(Emoji.fromUnicode(Config.declineUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.microphoneUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.trashUnicode)).queue();
                });

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final int maxLengthInOneMessage = 2000;
    public static void sendLinesWithoutReaction(String url, MessageChannelUnion messageChannel) {
        ArrayList<StringBuilder> messages = new ArrayList<>();
        messages.add(new StringBuilder());

        int currentStringBuilder = 0;

        try {
            JSONArray jsonArray = getJsonData(url);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String line = jsonObject.getString("message");

                if (messages.get(currentStringBuilder).length() + line.length() > maxLengthInOneMessage){
                    currentStringBuilder++;
                    messages.add(new StringBuilder());
                }
                messages.get(currentStringBuilder).append("\n").append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < messages.size(); i++){
            messageChannel.sendMessage("```" + messages.get(i) + "```").queue();
        }
    }

}
