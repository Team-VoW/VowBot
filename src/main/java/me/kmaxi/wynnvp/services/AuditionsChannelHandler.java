package me.kmaxi.wynnvp.services;

import me.kmaxi.wynnvp.BotRegister;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AuditionsChannelHandler {

    @Autowired
    private GuildService guildService;

    public void openAudition(String questName, String npcName, Member member) {

        System.out.println("Opening audition for " + npcName + " in " + questName + " for " + member.getUser().getName());

        TextChannel questChannel = getQuestChannel(questName, npcName);
        if (questChannel == null) {
            questChannel = createQuestChannel(questName, npcName);
        }

        User user = member.getUser();

        String channelName = npcName + "-" + user.getName().replace(".", "");

        Guild guild = guildService.getGuild();

        if (auditionThreadExists(channelName, questChannel)) {
            Utils.sendPrivateMessage(user, "You already have an application for " + npcName + " running. Type `?close` in the application channel to close it");
            return;
        }

        ThreadChannel threadChannel = questChannel.createThreadChannel(channelName, true)
                .setName(channelName)
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                .complete();

        threadChannel.addThreadMemberById(member.getIdLong()).queue();

        threadChannel.sendMessage("Thank you " + user.getAsMention() + " for applying for **" + npcName + "** in " + questName + "."
                + "\n \nPlease record the audition lines. If an entire script was provided, record the first three sentences that **" + npcName + "** says in the quest. "
                + "Record yourself performing the lines with recording software such as Audacity or this webpage https://vocaroo.com/. Next, send it in this channel. "
                + "After the audition closes, we will set up an internal voting poll to decide which person fits the role best."
                + "\n\n**Make sure** that you have read " + Objects.requireNonNull(guild.getGuildChannelById(823509081498451988L)).getAsMention() + " before applying. There are many useful tricks there and some must dos for voice acting!"
                + "\n\nTo delete this application simply say `?close`.This will not close your application but will prompt staff to close it."
                + "\n\nBy opening this application, you've agreed to the terms listed in " + Objects.requireNonNull(guild.getGuildChannelById(820027818799792129L)).getAsMention()
                + "\n\nGood luck and we can’t wait to hear your audition! If you have any questions feel free to ping " + Objects.requireNonNull(guild.getRoleById(Config.voiceMangerId)).getAsMention()).queue();
    }


    private boolean auditionThreadExists(String threadName, TextChannel channel) {
        return channel.getThreadChannels()
                .stream()
                .filter(thread -> thread.getName().equalsIgnoreCase(threadName))
                .findFirst()
                .orElse(null) != null;
    }

    private TextChannel getQuestChannel(String questName, String npcName) {

        String channelName = getChannelName(questName, npcName);
        Guild guild = guildService.getGuild();
        List<TextChannel> channels = Objects.requireNonNull(guild.getCategoryById(Config.applyCategoryId)).getTextChannels();

        return channels.stream()
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .findFirst().orElse(null);

    }


    private String getChannelName(String questName, String npcName) {
        return questName + "-" + npcName + "-Audition";
    }

    public void createQuestChannels(String questName, List<String> roles) {
        Guild guild = guildService.getGuild();

        for (String role : roles) {
            createQuestChannel(questName, role, guild.getCategoryById(Config.applyCategoryId));
        }
    }

    public TextChannel createQuestChannel(String questName, String role) {
        Guild guild = guildService.getGuild();

        return createQuestChannel(questName, role, guild.getCategoryById(Config.applyCategoryId));
    }

    private TextChannel createQuestChannel(String questName, String role, Category category) {
        Guild guild = guildService.getGuild();

        String channelName = getChannelName(questName, role);
        TextChannel channel = guild.createTextChannel(channelName, category)
                .setTopic("Auditions for " + role + " in " + questName)
                .complete();
        channel.sendMessage("Auditions for " + role + " in " + questName + "!").queue();
        return channel;
    }
}
