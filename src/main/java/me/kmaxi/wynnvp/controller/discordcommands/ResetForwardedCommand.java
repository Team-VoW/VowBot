package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.data.LineReportService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class ResetForwardedCommand implements ICommandImpl {

    @Autowired
    private LineReportService lineReportService;

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

        boolean successful = lineReportService.resetForwarded();

        if (successful) {
            event.reply("Resetting all forwarded lines was successful.").setEphemeral(true).queue();
        } else {
            event.reply("Failed to reset all forwarded lines. Check the log for more details.").setEphemeral(true).queue();
        }
    }
}
