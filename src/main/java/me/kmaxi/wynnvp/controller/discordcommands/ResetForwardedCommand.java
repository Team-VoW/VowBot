package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.data.LineReportService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResetForwardedCommand implements ICommandImpl {

    private final LineReportService lineReportService;

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
        log.info("Resetting all forwarded lines. Triggered by: " + event.getUser().getName());

        boolean successful = lineReportService.resetForwarded();

        if (successful) {
            event.reply("Resetting all forwarded lines was successful.").setEphemeral(true).queue();
        } else {
            event.reply("Failed to reset all forwarded lines. Check the log for more details.").setEphemeral(true).queue();
        }
    }
}
