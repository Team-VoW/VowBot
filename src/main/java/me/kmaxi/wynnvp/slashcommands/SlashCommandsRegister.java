package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.slashcommands.commands.*;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SlashCommandsRegister extends ListenerAdapter {


    Map<String, ICommandImpl> commands;

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

        List<ICommandImpl> commandData = new ArrayList<>();
        commandData.add(new HelpCommand());
        commandData.add(new CloseAuditionCommand());
        commandData.add(new CreateChannelCommand());
        commandData.add(new FinishedRoleCommand());
        commandData.add(new GetLinesCommand());
        commandData.add(new GetVotesCommand());
        commandData.add(new OpenCastingCommand());
        commandData.add(new ResetForwardedCommand());
        commandData.add(new RoleCommand());
        commandData.add(new SetupPollCommand());
        commandData.add(new SynAllUsersCommand());
        commandData.add(new VotesCommand());

        commands = new HashMap<>();
        for (ICommandImpl command : commandData) {
            commands.put(command.getCommandData().getName(), command);
        }

        // Register commands in the guild
        event.getGuild().updateCommands().addCommands(
                commandData.stream()
                        .map(ICommandImpl::getCommandData)
                        .collect(Collectors.toList())
        ).queue();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getMember() == null) {
            event.reply("Error. User was null").setEphemeral(true).queue();
            return;
        }
        String commandString = event.getName().toLowerCase().trim();

        if (!commands.containsKey(commandString)) {
            event.reply("Command not found").setEphemeral(true).queue();
            return;
        }

        ICommandImpl command = commands.get(commandString);

        if (command.getPermissionLevel() == PermissionLevel.STAFF && !Utils.isStaff(event.getMember())) {
            event.reply("You do not have permission do execute this command.").setEphemeral(true).queue();
            return;
        }

        if (command.getPermissionLevel() == PermissionLevel.ADMIN && !Utils.isAdmin(event.getMember())) {
            event.reply("You do not have permission do execute this command. You need Admin perms").setEphemeral(true).queue();
            return;
        }

        command.execute(event);
    }
}
