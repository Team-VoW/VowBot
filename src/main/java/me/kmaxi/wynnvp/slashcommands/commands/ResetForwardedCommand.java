package me.kmaxi.wynnvp.slashcommands.commands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static me.kmaxi.wynnvp.WynnVPBotMain.guild;

public class ResetForwardedCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("resetforwarded", "Moves all forwarded api entries back to index so they will be sent again.");
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        System.out.println("Reseting all forwarded");

        try {
            URL url = new URL("https://voicesofwynn.com/api/unvoiced-line-report/reset");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("PUT");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "&apiKey=" + APIKeys.updateApiKey;

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            int responseCode = http.getResponseCode();
            System.out.println(responseCode + " " + http.getResponseMessage());
            http.disconnect();
            event.reply("Response code for resetting lines was: " + responseCode).setEphemeral(true).queue();

        } catch (IOException e) {
            event.reply("Failed: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
