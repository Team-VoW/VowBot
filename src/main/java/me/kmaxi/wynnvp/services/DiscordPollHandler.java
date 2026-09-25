package me.kmaxi.wynnvp.services;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.services.audition.AuditionsChannelHandler;
import me.kmaxi.wynnvp.services.data.CastingService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ThreadMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Closes a Discord casting and moves it to the website for voting: the newest audio in every
 * audition thread is uploaded to the quest's casting round, where staff listen and vote.
 */
@Service
@Slf4j
public class DiscordPollHandler {
    private final AuditionsChannelHandler auditionsChannelHandler;
    private final CastingService castingService;
    private static final Set<String> SUPPORTED_AUDIO_FORMATS = Set.of(
            ".ogg", ".wav", ".mp3", ".m4a", ".aac", ".flac", ".opus",
            ".wma", ".aiff", ".alac", ".oga", ".webm", ".mp4", ".mov"
    );

    public DiscordPollHandler(AuditionsChannelHandler auditionsChannelHandler, CastingService castingService) {
        this.auditionsChannelHandler = auditionsChannelHandler;
        this.castingService = castingService;
    }

    public String setupPoll(String questName) {
        TextChannel questChannel = auditionsChannelHandler.getQuestChannel(questName);
        if (questChannel == null) {
            return "Failed opening auditions for " + questName + " because could not find quest channel";
        }

        Map<String, List<ThreadChannel>> auditionThreads = auditionsChannelHandler.getNpcThreadMap(questChannel);
        if (auditionThreads.isEmpty()) {
            return "No characters found in " + questChannel.getAsMention() + ", nothing to set up.";
        }

        CastingService.CastingRound round;
        try {
            round = castingService.ensureRound(questName, new ArrayList<>(auditionThreads.keySet()));
        } catch (RestClientException e) {
            log.error("Could not create the casting round for {}", questName, e);
            return "Could not reach the website to create the casting round: " + e.getMessage();
        }

        int uploaded = 0;
        int alreadyThere = 0;
        List<String> failed = new ArrayList<>();
        for (Map.Entry<String, List<ThreadChannel>> entry : auditionThreads.entrySet()) {
            String npcName = entry.getKey();
            for (ThreadChannel thread : entry.getValue()) {
                Optional<Message.Attachment> audition = findAuditionFile(thread);
                if (audition.isEmpty()) {
                    continue;
                }

                try {
                    CastingService.UploadedAudition result = uploadAudition(round.roundId(), npcName, thread, audition.get());
                    if (result.created()) {
                        uploaded++;
                    } else {
                        alreadyThere++;
                    }
                } catch (IOException | RestClientException e) {
                    log.error("Failed to upload the audition from thread {}", thread.getName(), e);
                    failed.add(thread.getAsMention());
                }
            }
        }

        StringBuilder reply = new StringBuilder()
                .append(round.created() ? "Created" : "Updated")
                .append(" the casting round for **").append(questName).append("** on the website: ")
                .append(uploaded).append(" auditions uploaded");
        if (alreadyThere > 0) {
            reply.append(", ").append(alreadyThere).append(" were already there");
        }
        reply.append(".\nOpen it for voting here: ").append(round.adminUrl());
        if (!failed.isEmpty()) {
            reply.append("\nFailed to upload: ").append(String.join(", ", failed)).append(". Run the command again to retry.");
        }
        return reply.toString();
    }

    private CastingService.UploadedAudition uploadAudition(int roundId,
                                                           String npcName,
                                                           ThreadChannel thread,
                                                           Message.Attachment attachment) throws IOException {
        byte[] audio;
        try (InputStream in = attachment.getProxy().download().join()) {
            audio = in.readAllBytes();
        }

        Optional<User> auditionee = findAuditionee(thread, npcName);
        String auditioneeName = auditionee.map(User::getName).orElseGet(() -> auditioneeFromThreadName(thread, npcName));
        String discordUserId = auditionee.map(User::getId).orElse(null);

        return castingService.uploadAudition(
                roundId, npcName, auditioneeName, discordUserId, thread.getId(), audio, attachment.getFileName());
    }

    /**
     * Audition threads are named {@code <npc>-<username>} and the auditionee is added as a member,
     * so the member whose name matches the suffix is the one who auditioned.
     */
    private Optional<User> findAuditionee(ThreadChannel thread, String npcName) {
        String expected = auditioneeFromThreadName(thread, npcName);
        try {
            return thread.retrieveThreadMembers().complete().stream()
                    .map(ThreadMember::getUser)
                    .filter(user -> !user.isBot())
                    .filter(user -> Utils.getChannelName(user.getName()).equalsIgnoreCase(expected))
                    .findFirst();
        } catch (RuntimeException e) {
            log.warn("Could not list the members of thread {}", thread.getName(), e);
            return Optional.empty();
        }
    }

    private String auditioneeFromThreadName(ThreadChannel thread, String npcName) {
        String prefix = Utils.getChannelName(npcName.toLowerCase() + "-");
        String name = thread.getName().toLowerCase();
        return name.startsWith(prefix) ? thread.getName().substring(prefix.length()) : thread.getName();
    }

    /**
     * Find the latest audio file that was sent in there. Reply to that message in the channel saying that this audio was used for the audition.
     * If no audio was found send a message in that channel saying that they did not submit an audition in time.
     *
     * @param threadChannel the thread channel to search in
     * @return the audition file if found
     */
    private Optional<Message.Attachment> findAuditionFile(ThreadChannel threadChannel) {
        List<Message> messages = Utils.getMessageHistory(threadChannel, 100); // Fetch the latest 100 messages
        for (Message message : messages) {
            for (Message.Attachment attachment : message.getAttachments()) {
                String fileName = attachment.getFileName().toLowerCase();
                if (SUPPORTED_AUDIO_FORMATS.stream().anyMatch(fileName::endsWith)) {
                    message.reply("This audio was used for the audition. If this was a mistake please ping a staff member as soon as possible.").queue();
                    return Optional.of(attachment);
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

        return Optional.empty();
    }
}
