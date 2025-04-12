package me.kmaxi.wynnvp.services;


import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.LineReportData;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.services.data.LineReportService;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static me.kmaxi.wynnvp.BotRegister.guild;
import static me.kmaxi.wynnvp.utils.APIUtils.getJsonData;

@Service
public class LineReportHandler {

    @Autowired
    private LineReportService lineReportService;

    public void sendLinesWithReaction(LineType type, String npcName, MessageChannelUnion messageChannel) {

        List<LineReportData> messages = lineReportService.fetchMessages(type, npcName);

        for (LineReportData message : messages) {
            messageChannel.sendMessage(message.getMessage()).queue(message1 -> {
                message1.addReaction(Emoji.fromUnicode(Config.declineUnicode)).queue();
                message1.addReaction(Emoji.fromUnicode(Config.microphoneUnicode)).queue();
                message1.addReaction(Emoji.fromUnicode(Config.trashUnicode)).queue();
            });
        }
    }

    public void sendLinesWithoutReaction(LineType type, String npcName, MessageChannelUnion messageChannel) {
        List<LineReportData> messages = lineReportService.fetchMessages(type, npcName);
        ArrayList<StringBuilder> messageChunks = new ArrayList<>();
        messageChunks.add(new StringBuilder());

        int currentChunkIndex = 0;
        int maxLengthInOneMessage = 2000;

        for (LineReportData message : messages) {
            String line = message.getMessage();

            if (messageChunks.get(currentChunkIndex).length() + line.length() > maxLengthInOneMessage) {
                currentChunkIndex++;
                messageChunks.add(new StringBuilder());
            }
            messageChunks.get(currentChunkIndex).append("\n").append(line);
        }

        for (StringBuilder chunk : messageChunks) {
            messageChannel.sendMessage("```" + chunk + "```").queue();
        }
    }

    public void sendAllNewReports() {
        try {
            JSONArray jsonArray = getJsonData("http://voicesofwynn.com/api/unvoiced-line-report/index?apiKey=" + APIKeys.readingApiKey);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

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
