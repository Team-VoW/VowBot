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
import java.io.IOException;
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
    private final AudioConversionService audioConversionService;
    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Set.of(
            ".ogg", ".wav", ".mp3", ".m4a", ".aac", ".flac", ".opus",
            ".wma", ".aiff", ".alac", ".oga", ".webm", ".mp4", ".mov"
    );

    public DiscordPollHandler(AuditionsChannelHandler auditionsChannelHandler, GuildService guildService, AudioConversionService audioConversionService) {
        this.auditionsChannelHandler = auditionsChannelHandler;
        this.guildService = guildService;
        this.audioConversionService = audioConversionService;
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
        File tempFile = null;
        File convertedFile = null;

        try {
            tempFile = downloadAudioFile(fileUrl, thread.getName());
            convertedFile = convertAudioFile(tempFile, thread.getName());
            sendAudioToStaffChannel(convertedFile, thread, staffVotingChannel);

        } catch (Exception e) {
            log.error("Failed to download or send the audition file from thread: {}", thread.getAsMention(), e);
            staffVotingChannel.sendMessage("Failed to download or send the audition file from thread: " + thread.getAsMention()).queue();
            cleanupTempFiles(tempFile, convertedFile);
        }
    }

    /**
     * Downloads an audio file from a URL to a temporary location.
     *
     * @param fileUrl the URL of the file to download
     * @param threadName the name of the thread (used for file naming)
     * @return the downloaded file
     * @throws IOException if download fails
     */
    private File downloadAudioFile(String fileUrl, String threadName) throws IOException {
        URL url = new URL(fileUrl);

        // Remove query parameters before extracting extension
        String originalFileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1).split("\\?")[0];
        String extension = originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf('.'))
                : ".tmp";

        // Sanitize thread name for filesystem
        String sanitizedThreadName = threadName.replaceAll("[\\\\/:*?\"<>|]", "_");

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

        return tempFile;
    }

    /**
     * Converts an audio file to MP3 format.
     *
     * @param tempFile the file to convert
     * @param threadName the thread name for logging
     * @return the converted file (or original if conversion fails)
     */
    private File convertAudioFile(File tempFile, String threadName) {
        try {
            File convertedFile = audioConversionService.convertToMp3(tempFile);
            log.info("Converted audio file for thread {} to MP3", threadName);
            return convertedFile;
        } catch (Exception e) {
            log.warn("Failed to convert audio to MP3, using original file: {}", e.getMessage());
            return tempFile; // Fall back to original file if conversion fails
        }
    }

    /**
     * Sends an audio file to the staff voting channel.
     *
     * @param fileToSend the file to send
     * @param thread the thread channel
     * @param staffVotingChannel the staff voting channel
     */
    private void sendAudioToStaffChannel(File fileToSend, ThreadChannel thread, TextChannel staffVotingChannel) {
        staffVotingChannel.sendMessage(thread.getAsMention())
                .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(fileToSend))
                .queue(
                        success -> handleSendSuccess(fileToSend, success),
                        error -> handleSendError(fileToSend, thread, staffVotingChannel)
                );
    }

    /**
     * Handles successful file send.
     *
     * @param fileToSend the file that was sent
     * @param message the sent message
     */
    private void handleSendSuccess(File fileToSend, net.dv8tion.jda.api.entities.Message message) {
        deleteFile(fileToSend);
        message.addReaction(Emoji.fromUnicode("✅")).queue();
    }

    /**
     * Handles failed file send.
     *
     * @param fileToSend the file that failed to send
     * @param thread the thread channel
     * @param staffVotingChannel the staff voting channel
     */
    private void handleSendError(File fileToSend, ThreadChannel thread, TextChannel staffVotingChannel) {
        deleteFile(fileToSend);
        staffVotingChannel.sendMessage("Failed to send the audition file from thread: " + thread.getAsMention()).queue();
    }

    /**
     * Deletes a file and logs any errors.
     *
     * @param file the file to delete
     */
    private void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (Exception e) {
            log.error("Failed to delete temporary file: {}", file.getAbsolutePath(), e);
        }
    }

    /**
     * Cleans up temporary files.
     *
     * @param tempFile the original temp file
     * @param convertedFile the converted file
     */
    private void cleanupTempFiles(File tempFile, File convertedFile) {
        if (tempFile != null && tempFile.exists()) {
            deleteFile(tempFile);
        }
        if (convertedFile != null && convertedFile.exists() && !convertedFile.equals(tempFile)) {
            deleteFile(convertedFile);
        }
    }


}
