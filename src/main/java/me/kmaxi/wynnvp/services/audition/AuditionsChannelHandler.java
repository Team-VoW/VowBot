package me.kmaxi.wynnvp.services.audition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.GuildService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditionsChannelHandler {

    private final GuildService guildService;

    private final AuditionThreadHandler auditionThreadHandler;

    public void openAudition(String questName, String npcName, Member member) {

        log.info("Opening audition for {} in {} for {}", npcName, questName, member.getUser().getName());

        TextChannel questChannel = getQuestChannel(questName, npcName);
        if (questChannel == null) {
            questChannel = createQuestChannel(questName, npcName);
        }

        User user = member.getUser();
        auditionThreadHandler.createThreadIfNotExists(user, npcName, questName, questChannel);
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
        return trimForChannelName(questName) + "-" + trimForChannelName(npcName) + "-Audition";
    }

    private String trimForChannelName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-]", "");
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
