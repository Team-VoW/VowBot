package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ApiCommands {

    public static void getAcceptedLinesFromCharacter(SlashCommandInteractionEvent event) {

        OptionMapping addReactionOption = event.getOption("addreaction");
        boolean addReaction = addReactionOption != null && addReactionOption.getAsBoolean();

        String url = "https://voicesofwynn.com/api/unvoiced-line-report/accepted?npc="
                + event.getOption("npcname").getAsString() + "&apiKey=" + Config.readingApiKey;

        event.reply("Sending lines now").setEphemeral(true).queue();


        LineReportManager.SendAllLinesFromCharacter(url, event.getMessageChannel(), addReaction);
    }
}
