package me.kmaxi.wynnvp;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.services.GuildService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class BotRegister {
    private final GuildService guildService;
    private final List<ListenerAdapter> eventListeners;
    private final APIKeys apiKeys;

    public BotRegister(GuildService guildService, List<ListenerAdapter> eventListeners, APIKeys apiKeys) {
        this.guildService = guildService;
        this.apiKeys = apiKeys;
        this.eventListeners = eventListeners;
    }

    @Bean
    public JDA jda() throws InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(apiKeys.botToken,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.MESSAGE_CONTENT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        for (Object listener : eventListeners) {
            builder.addEventListeners(listener);
        }

        JDA jda = builder.build();
        jda.awaitReady();
        log.info("JDA is ready!");
        jda.updateCommands().queue();
        guildService.setGuild(jda.getGuildById(814401551292563477L));
        return jda;
    }
}