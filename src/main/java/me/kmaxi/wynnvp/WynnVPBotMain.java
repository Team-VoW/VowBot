package me.kmaxi.wynnvp;

import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.listeners.AddEmoteListener;
import me.kmaxi.wynnvp.slashcommands.SlashCommandsRegister;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;

import javax.security.auth.login.LoginException;

public class WynnVPBotMain {

    //Test bot key: ODIzMjc1ODc5NDM3ODI4MTE2.YFedaQ.HD2kvFNQdNUUhsMe2sbfiKvOeMU


    public static Guild guild;
    public static void main(String[] args) {
        try {
            JDA jda = JDABuilder.createDefault(Config.BotToken)
                    .addEventListeners(new AddEmoteListener())
                    .addEventListeners(new SlashCommandsRegister())
                    .build();
            jda.awaitReady();
            System.out.println("Finished building JDA!");
            LineReportManager.startTimer();
            jda.updateCommands().queue();
            guild = jda.getGuildById(814401551292563477L);
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }


    }



}

