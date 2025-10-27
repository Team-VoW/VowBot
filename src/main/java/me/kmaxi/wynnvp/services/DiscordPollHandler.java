package me.kmaxi.wynnvp.services;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.services.audition.AuditionsChannelHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class DiscordPollHandler {
    private final AuditionsChannelHandler auditionsChannelHandler;
    private final GuildService guildService;
    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Set.of(".ogg", ".wav", ".mp3", ".m4a");

    public DiscordPollHandler(AuditionsChannelHandler auditionsChannelHandler, GuildService guildService) {
        this.auditionsChannelHandler = auditionsChannelHandler;
        this.guildService = guildService;
    }

    public String setupPoll(String questName) {
        TextChannel questChannel = auditionsChannelHandler.getQuestChannel(questName);
        if (questChannel == null) {
            return "Failed opening auditions for " + questName + " because could not find quest channel";
        }

        Map<String, List<ThreadChannel>> auditionMessages = auditionsChannelHandler.getNpcThreadMap(questChannel);

        TextChannel staffVotingChannel = guildService.getStaffVotingChannel();

        if (staffVotingChannel == null) {
            return "Failed opening auditions for " + questName + " because could not find staff voting channel";
        }

        staffVotingChannel.sendMessage("# Poll for " + questName).queue();
        for (Map.Entry<String, List<ThreadChannel>> entry : auditionMessages.entrySet()) {
            String npcName = entry.getKey();
            List<ThreadChannel> threads = entry.getValue();
            staffVotingChannel.sendMessage("## " + npcName).queue();

            for (ThreadChannel thread : threads) {
                String auditionFileLink = getAuditionFileLink(thread);
                if (auditionFileLink == null) {
                    continue;
                }

                // Download the file to a temporary location and then send a message that has the
                //content of thread.getAsMention() and the audio file as an attachement.
                // THen delete the file.
                forwardAuditionToStaffChannel(thread, auditionFileLink, staffVotingChannel);


            }
        }
        return "Poll setup complete for " + questName;

    }

    /**
     * Find the latest audio file that was sent in there. Reply to that message in the channel saying that this audio was used for the audition.
     * If no audio was found send a message in that channel saying that they did not submit an audition in time.
     *
     * @param threadChannel the thread channel to search in
     * @return the URL of the audition file if found, null otherwise
     */
    private String getAuditionFileLink(ThreadChannel threadChannel) {
        List<Message> messages = Utils.getMessageHistory(threadChannel, 100); // Fetch the latest 100 messages
        for (Message message : messages) {
            if (!message.getAttachments().isEmpty()) {
                for (Message.Attachment attachment : message.getAttachments()) {
                    String fileName = attachment.getFileName().toLowerCase();
                    if (SUPPORTED_AUDIO_FORMATS.stream().anyMatch(fileName::endsWith)) {
                        message.reply("This audio was used for the audition. If this was a mistake please ping a staff member as soon as possible.").queue();
                        return attachment.getUrl();
                    }
                }
            }
        }
        if (messages.size() == 100) {
            threadChannel.sendMessage("Audition file not found in the last 100 messages. If one was sent previously please ping a staff member as soon as possible. " +
                    "You can not submit any new recordings since the internal voting process has now started.").queue();
        } else {
            threadChannel.sendMessage("Audition file not found. If one was sent please ping a staff member as soon as possible. " +
                    "You can now not submit any new recordings since the internal voting process has now started.").queue();
        }

        return null;
    }

    private void forwardAuditionToStaffChannel(ThreadChannel thread, String fileUrl, TextChannel staffVotingChannel) {
        try {
            URL url = new URL(fileUrl);

            // Remove query parameters before extracting extension
            String originalFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1).split("\\?")[0];
            String extension = originalFileName.contains(".")
                    ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                    : ".tmp";

            // Sanitize thread name for filesystem
            String sanitizedThreadName = thread.getName().replaceAll("[\\\\/:*?\"<>|]", "_");

            // Build file path in temp dir
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File tempFile = new File(tempDir, sanitizedThreadName + extension);

            // Download the file
            try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Send the file with the thread mention
            staffVotingChannel.sendMessage(thread.getAsMention())
                    .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(tempFile))
                    .queue(
                            success -> {
                                try {
                                    Files.delete(tempFile.toPath());
                                    success.addReaction(Emoji.fromUnicode("✅")).queue();
                                } catch (Exception e) {
                                    log.error("Failed to delete temporary file: " + tempFile.getAbsolutePath(), e);
                                }
                            },
                            error -> {
                                try {
                                    Files.delete(tempFile.toPath());
                                } catch (Exception e) {
                                    log.error("Failed to delete temporary file: " + tempFile.getAbsolutePath(), e);
                                }
                                staffVotingChannel.sendMessage("Failed to send the audition file from thread: " + thread.getAsMention()).queue();
                            }
                    );

        } catch (Exception e) {
            log.error("Failed to download or send the audition file from thread: " + thread.getAsMention(), e);
            staffVotingChannel.sendMessage("Failed to download or send the audition file from thread: " + thread.getAsMention()).queue();
        }
    }


}
