package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("help", "Get all vow bot commands");
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ANYONE;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply(("""
                `/close soon` to close an application.
                `/close immediately` to close an application directly.
                `/role set <QuestName> <NpcName> <PersonWhoGotRole>` to assign a role to a person
                `/role open <QuestName> <NpcName>(CaseSens)>` to remove an assignation
                `/role open <QuestName> <NpcName>(CaseSens)>` to remove an assignation
                `/setuppoll to set up the voting for either CCC casting or discord casting`
                `/finishedrole upgrades the users role here and on the website`
                `/opencasting <QuestName> <Npc> <Npc>...` to add a new quest. Maximum 9 roles.
                `/resetforwarded` sets all lines with status unproccesed to forwarded. WARNING! Clear reported lines channel before doing this!`
                /getlines <Npc>` to get lines from a npc""")).setEphemeral(true).queue();
    }
}