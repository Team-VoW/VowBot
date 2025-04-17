package me.kmaxi.wynnvp.services.audition;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AuditionThreadHandler {

    private final GuildService guildService;

    public void createThreadIfNotExists(User user, String npcName, String questName, TextChannel questChannel) {

        String channelName = npcName + "-" + user.getName().replace(".", "");


        if (auditionThreadExists(channelName, questChannel)) {
            Utils.sendPrivateMessage(user, "You already have an application for " + npcName + " running. Type `?close` in the application channel to close it");
            return;
        }

        ThreadChannel threadChannel = questChannel.createThreadChannel(channelName, true)
                .setName(channelName)
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                .complete();

        threadChannel.addThreadMemberById(user.getIdLong()).queue();

        Guild guild = guildService.getGuild();

        threadChannel.sendMessage("Thank you " + user.getAsMention() + " for applying for **" + npcName + "** in " + questName + "."
                + "\n \nPlease record the audition lines. If an entire script was provided, record the first three sentences that **" + npcName + "** says in the quest. "
                + "Record yourself performing the lines with recording software such as Audacity or this webpage https://vocaroo.com/. Next, send it in this channel. "
                + "After the audition closes, we will set up an internal voting poll to decide which person fits the role best."
                + "\n\n**Make sure** that you have read " + Objects.requireNonNull(guild.getGuildChannelById(823509081498451988L)).getAsMention() + " before applying. There are many useful tricks there and some must dos for voice acting!"
                + "\n\nTo delete this application simply say `?close`.This will not close your application but will prompt staff to close it."
                + "\n\nBy opening this application, you've agreed to the terms listed in " + Objects.requireNonNull(guild.getGuildChannelById(820027818799792129L)).getAsMention()
                + "\n\nGood luck and we can’t wait to hear your audition! If you have any questions feel free to ping " + Objects.requireNonNull(guild.getRoleById(Config.VOICE_MANGER_ID)).getAsMention()).queue();
    }

    private boolean auditionThreadExists(String threadName, TextChannel channel) {
        return channel.getThreadChannels()
                .stream()
                .filter(thread -> thread.getName().equalsIgnoreCase(threadName))
                .findFirst()
                .orElse(null) != null;
    }
}
