package me.kmaxi.wynnvp.services.audition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuditionThreadHandler {

    private final GuildService guildService;

    public Optional<ThreadChannel> createThreadIfNotExists(User user, String npcName, String questName, TextChannel questChannel) {

        // Add null check for questChannel
        if (questChannel == null) {
            log.error("Cannot create thread for {} - questChannel is null for quest: {}", npcName, questName);
            Utils.sendPrivateMessage(user, "Error: Could not find the audition channel for " + questName + ". Please contact a staff member.");
            return Optional.empty();
        }

        String channelName = Utils.getChannelName(npcName + "-" + user.getName());


        if (auditionThreadExists(channelName, questChannel)) {
            Utils.sendPrivateMessage(user, "You already have an application for " + npcName + " running. Type `ABANDON` in the application channel to remove it.");
            log.info("{} tried to open a thread channel while already having a channel with the name {}", user.getName(), channelName);
            return Optional.empty();
        }

        ThreadChannel threadChannel = questChannel.createThreadChannel(channelName, true)
                .setName(channelName)
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                .complete();

        threadChannel.addThreadMemberById(user.getIdLong()).queue();

        Guild guild = guildService.getGuild();

        threadChannel.sendMessage("Thank you **" + user.getName() + "** for applying for **" + npcName + "** in " + questName + "."
                + "\n\nPlease record the audition lines. If an entire script was provided, record the first 3 sentences that **" + npcName + "** says in the quest. "
                + "You can find the script in " + Objects.requireNonNull(questChannel).getAsMention() + ". "
                + "\nRecord yourself performing the lines with recording software such as [Audacity](https://www.audacityteam.org/download/) (download required) or [Vocaroo](https://vocaroo.com/) (online webapp). Next, send it in this channel (in case of Vocaroo, download the file and send it here, don't send the link only). "
                + "If the file exceeds the Discord upload limit, convert it to the OGG format (smaller filesize), send that and keep the original WAV file on your computer only. You'll be asked for the original file if you get picked for this role."
                + "\nAfter the audition closes, we will set up an internal voting poll to decide which person fits the role best."
                + "\n\n**Make sure** that you have read " + Objects.requireNonNull(guild.getGuildChannelById(823509081498451988L)).getAsMention() + " before applying. There are many useful tricks there and some must dos for voice acting!"
                + "\n\nTo delete this application simply send `ABANDON` in this thread. This thread will then be deleted by a staff member, once such message is noticed."
                + "\n\nBy opening this application, you've agreed to the terms listed in " + Objects.requireNonNull(guild.getGuildChannelById(820027818799792129L)).getAsMention()
                + "\n\nGood luck and we can’t wait to hear your audition! Once you submit your audition, feel free to ping `@Audition Guide` to get potentional feedback. This isn't required though.").queue();

        return Optional.of(threadChannel);
    }

    private boolean auditionThreadExists(String threadName, TextChannel channel) {
        return channel.getThreadChannels()
                .stream()
                .filter(thread -> thread.getName().equalsIgnoreCase(threadName))
                .findFirst()
                .orElse(null) != null;
    }
}
