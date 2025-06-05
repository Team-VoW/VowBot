package me.kmaxi.wynnvp.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.LineReportDTO;
import me.kmaxi.wynnvp.dtos.VowDialogueDTO;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.enums.SetLinesCommand;
import me.kmaxi.wynnvp.services.data.LineReportService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineReportHandler {

    private final LineReportService lineReportService;

    private final GuildService guildService;

    public void sendLinesWithoutReaction(LineType type, String npcName, MessageChannelUnion messageChannel) {
        List<LineReportDTO> messages = lineReportService.fetchMessages(type, npcName);
        ArrayList<StringBuilder> messageChunks = new ArrayList<>();
        messageChunks.add(new StringBuilder());

        int currentChunkIndex = 0;
        int maxLengthInOneMessage = 2000;

        for (LineReportDTO message : messages) {
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

        List<LineReportDTO> messages = lineReportService.getNewReports();

        for (LineReportDTO messageDTO : messages) {
            String forwardedMessage = "\uD83E\uDDD1\u200D\uD83C\uDF3E `" + messageDTO.getNpc() + "`\n"
                    + "\uD83D\uDDFA `" + messageDTO.getX() + "|" + messageDTO.getY() + "|" + messageDTO.getZ() + "`\n"
                    + "\uD83D\uDCE3 `" + messageDTO.getReporter() + "`\n"
                    + "> `" + messageDTO.getMessage() + "`";

            TextChannel channel = guildService.getGuild().getTextChannelById(Config.REPORTED_LINES);
            if (channel == null) {
                log.error("Channel {} not found", Config.REPORTED_LINES);
                return;
            }

            log.info("Sending report of message: " + messageDTO.getMessage());

            channel.sendMessage(forwardedMessage).queue(message1 -> {
                message1.addReaction(Emoji.fromUnicode(Config.ACCEPT_UNICODE)).queue();
                message1.addReaction(Emoji.fromUnicode(Config.DECLINE_UNICODE)).queue();
                message1.addReaction(Emoji.fromUnicode(Config.MICROPHONE_UNICODE)).queue();
                message1.addReaction(Emoji.fromUnicode(Config.TRASH_UNICODE)).queue();
            });
        }
    }

    public String setLinesAsVoiced(Message.Attachment attachment) {
        if (!Objects.equals(attachment.getFileExtension(), "json")) {
            return "The file is not a JSON file.";
        }

        try {
            // Download the file content
            byte[] fileBytes = attachment.getProxy().download().get().readAllBytes();
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

            // Parse the JSON array into a list of VowDialogueDTO objects
            ObjectMapper objectMapper = new ObjectMapper();
            List<VowDialogueDTO> dialogues = objectMapper.readValue(fileContent, new TypeReference<>() {
            });

            if (dialogues.isEmpty()) {
                return "The file is empty or does not contain valid dialogue data.";
            }

            boolean successful = lineReportService.setLinesAsVoiced(dialogues, SetLinesCommand.VOICED);

            if (successful) {
                return "File processed successfully. Parsed " + dialogues.size() + " dialogues.";
            } else {
                return "Some or all requests to update failed.";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Operation interrupted: {}", e.getMessage(), e);
            return "Operation was interrupted: " + e.getMessage();
        } catch (Exception e) {
            log.error("Failed to process the file: {}", e.getMessage(), e);
            return "Failed to process the file: " + e.getMessage();
        }
    }


}
