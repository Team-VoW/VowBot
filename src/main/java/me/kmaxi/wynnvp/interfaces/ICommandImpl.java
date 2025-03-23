package me.kmaxi.wynnvp.interfaces;

import me.kmaxi.wynnvp.PermissionLevel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommandImpl {
    CommandData getCommandData();
    PermissionLevel getPermissionLevel();
    void execute(SlashCommandInteractionEvent event);

}
