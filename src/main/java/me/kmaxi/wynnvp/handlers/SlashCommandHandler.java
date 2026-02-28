package me.kmaxi.wynnvp.handlers;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlashCommandHandler extends ListenerAdapter {

    private final Map<String, ICommandImpl> commands = new HashMap<>();

    public SlashCommandHandler(List<ICommandImpl> commandImplementations) {
        for (ICommandImpl command : commandImplementations) {
            commands.put(command.getCommandData().getName(), command);
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                commands.values().stream()
                        .map(ICommandImpl::getCommandData)
                        .toList()
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getMember() == null) {
            event.reply("Error. User was null").setEphemeral(true).queue();
            return;
        }
        String commandString = event.getName().toLowerCase().trim();

        ICommandImpl command = commands.get(commandString);
        if (command == null) {
            event.reply("Command not found").setEphemeral(true).queue();
            return;
        }

        if (!command.hasPermission(event.getMember())) {
            String suffix = command.getPermissionLevel() == PermissionLevel.ADMIN ? " You need Admin perms" : "";
            event.reply("You do not have permission to execute this command." + suffix).setEphemeral(true).queue();
            return;
        }

        command.execute(event);
    }
}
