package me.kmaxi.wynnvp;

import me.kmaxi.wynnvp.listeners.AddEmoteListener;
import me.kmaxi.wynnvp.listeners.ChatListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class WynnVPBotMain {

    //Wynn Vp key: ODIxMzk3MDIyMjUwMzY5MDU0.YFDHlg.U9gV5RgFyviEMIUzel6vAJV7ugY
    //Test bot key: ODIzMjc1ODc5NDM3ODI4MTE2.YFedaQ.HD2kvFNQdNUUhsMe2sbfiKvOeMU


    public static void main(String[] args) {
        try {
            JDA jda = JDABuilder.createDefault(Config.BotToken)
                    .addEventListeners(new ChatListener())
                    .addEventListeners(new AddEmoteListener())
                    .build();
            jda.awaitReady();
            System.out.println("Finished building JDA!");

        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}

