package me.kmaxi.wynnvp.linereport;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import static me.kmaxi.wynnvp.BotRegister.guild;
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





}
