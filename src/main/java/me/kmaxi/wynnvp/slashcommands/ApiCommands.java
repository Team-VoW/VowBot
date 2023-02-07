package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ApiCommands {

    public static void getAcceptedLinesFromCharacter(SlashCommandInteractionEvent event) {
        getLines("accepted", event);
    }

    public static void getActiveLinesFromCharacter(SlashCommandInteractionEvent event) {
        getLines("active", event);
    }
    public static void getAllLinesFromCharacter(SlashCommandInteractionEvent event) {
        getLines("valid", event);
    }

    private static void getLines(String apiKeyword, SlashCommandInteractionEvent event){
        OptionMapping addReactionOption = event.getOption("addreaction");
        boolean addReaction = addReactionOption != null && addReactionOption.getAsBoolean();
        String url = getReadingUrl(apiKeyword, event);

        event.reply("Sending lines now").setEphemeral(true).queue();

        if (addReaction){
            LineReportManager.sendLinesWithReaction(url, event.getMessageChannel());
        } else {
            LineReportManager.sendLinesWithoutReaction(url, event.getMessageChannel());
        }
    }

    private static String getReadingUrl(String keyword, SlashCommandInteractionEvent event){
        return "https://voicesofwynn.com/api/unvoiced-line-report/" + keyword + "?npc="
                + event.getOption("npcname").getAsString().replace(" ", "%20") + "&apiKey=" + APIKeys.readingApiKey;
    }
}
