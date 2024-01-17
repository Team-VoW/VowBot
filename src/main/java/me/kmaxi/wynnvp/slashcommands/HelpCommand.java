package me.kmaxi.wynnvp.slashcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand {

    public static void TriggerCommand(SlashCommandInteractionEvent event) {
        event.reply("`/close` to close an application."
                + "\n`/setRole <QuestName> <NpcName> <PersonWhoGotRole>` to assign a role to a person"
                + "\n`/openrole <QuestName> <NpcName>(CaseSens)>` to remove an assignation"
                + "\n`/addquest <QuestName> <Npc> <Npc>...` to add a new quest. Maximum 9 roles."
                + "\n`/resetforwarded` sets all lines with status unproccesed to forwarded. WARNING! Clear reported lines channel before doing this!"
                + "\n`/getacceptedlines <Npc>` to get all accepted lines from a npc"
                + "\n`/getactivelines <Npc>` to get all accepted and reported lines from a npc"
                + "\n`/getalllines <Npc>` to get every single line ever reported from an npc").setEphemeral(true).queue();


    }
}
