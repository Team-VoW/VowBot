package me.kmaxi.wynnvp.interfaces;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommandImpl {
    CommandData getCommandData();
    PermissionLevel getPermissionLevel();
    void execute(SlashCommandInteractionEvent event);

    default boolean hasPermission(Member member) {
        return switch (getPermissionLevel()) {
            case ANYONE -> true;
            case STAFF -> Utils.isStaff(member);
            case ADMIN -> Utils.isAdmin(member);
        };
    }
}
