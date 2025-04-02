package me.kmaxi.wynnvp.services;


import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.LineReportData;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.services.data.LineReportService;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

}
