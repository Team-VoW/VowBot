package me.kmaxi.wynnvp;

import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.listeners.AddEmoteListener;
import me.kmaxi.wynnvp.listeners.ButtonClickedListener;
import me.kmaxi.wynnvp.listeners.TokenActivationHandler;
import me.kmaxi.wynnvp.slashcommands.SlashCommandsRegister;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class WynnVPBotMain {
    public static Guild guild;
    @Bean
    public JDA jda() throws LoginException, InterruptedException {
        JDA jda = JDABuilder.createDefault(APIKeys.BotToken,
                        // Enabled events
                        GatewayIntent.GUILD_MEMBERS,// Enabling member events (Member join, leave, ...)
                        GatewayIntent.GUILD_MESSAGES, // Enabling message events (send, edit, delete, ...)
                        GatewayIntent.GUILD_MESSAGE_REACTIONS, // Enabling message reaction events (add, remove, ...)
                        GatewayIntent.GUILD_VOICE_STATES, // Enabling voice events (join, leave, mute, deafen, ...)
                        //GatewayIntent.GUILD_PRESENCES, // Is needed for the CLIENT_STATUS CacheFlag
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS) // Enabling emote events (add, update, delete, ...). Also is needed for the CacheFlag.EMOTE)

                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new AddEmoteListener())
                .addEventListeners(new ButtonClickedListener())
                .addEventListeners(new TokenActivationHandler())
                .addEventListeners(new SlashCommandsRegister())
                .build();
        jda.awaitReady();
        System.out.println("Finished building JDA!");
        LineReportManager.startTimer();
        jda.updateCommands().queue();
        guild = jda.getGuildById(814401551292563477L);
        return jda;

    }
}