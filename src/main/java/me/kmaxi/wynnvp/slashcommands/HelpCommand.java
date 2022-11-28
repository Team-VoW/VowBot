package me.kmaxi.wynnvp.slashcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand {

    public static void TriggerCommand(SlashCommandInteractionEvent event) {
        event.reply(("`?close` to close an application."
                + "\n`?setRole <QuestName> <NpcName> <PersonWhoGotRole(CaseSens)>` to assign a role to a person"
                + "\n`?removeRole <QuestName> <NpcName>(CaseSens)>` to remove an assignation"
                + "\n`?addquest <QuestName> <Npc> <Npc>...` to add a new quest. Maximum 9 roles." +
                "\n`?resetf` sets all lines with status unproccesed to forwarded. WARNING! Clear reported lines channel before doing this!"
                + "`\n ?getLines <Npc>` to get all accepted lines from a npc"
                + "`\n ?getLinesReact <Npc>` to get all accepted lines from a npc with reaction options"
                + "`\n ?purgereports` clears all messages in reported lines channel")).setEphemeral(true).queue();


    }
}
