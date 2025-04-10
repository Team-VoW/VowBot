package me.kmaxi.wynnvp;

import me.kmaxi.wynnvp.linereport.LineReportManager;

import me.kmaxi.wynnvp.services.GuildService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;
import java.util.List;

@Configuration
public class BotRegister {

    public static Guild guild;

    @Autowired
    private GuildService guildService;

    @Autowired
    private List<ListenerAdapter> eventListeners;

    @Bean
    public JDA jda() throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(APIKeys.BotToken,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        for (Object listener : eventListeners) {
            builder.addEventListeners(listener);
        }

        JDA jda = builder.build();
        jda.awaitReady();
        System.out.println("Finished building JDA!");
        LineReportManager.startTimer();
        jda.updateCommands().queue();
        guild = jda.getGuildById(814401551292563477L);
        guildService.setGuild(jda.getGuildById(814401551292563477L));
        return jda;
    }
}