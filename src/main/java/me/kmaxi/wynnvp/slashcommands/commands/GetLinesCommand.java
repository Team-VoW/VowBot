package me.kmaxi.wynnvp.slashcommands.commands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

@Component
public class GetLinesCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("getlines", "Sends lines based on the specified type in the current channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc", true),
                        new OptionData(OptionType.BOOLEAN, "addreaction", "If it should send the messages one at a time to allow reaction.", false),
                        new OptionData(OptionType.STRING, "type", "The type of lines to retrieve (accepted, active, all)", false)
                                .addChoice("accepted", "accepted")
                                .addChoice("active", "active")
                                .addChoice("all", "all"));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        String type = (typeOption != null) ? typeOption.getAsString() : "all";
        String apiKeyword = getApiKeyword(type);

        OptionMapping addReactionOption = event.getOption("addreaction");
        boolean addReaction = addReactionOption != null && addReactionOption.getAsBoolean();
        String url = getReadingUrl(apiKeyword, event);

        event.reply("Sending lines now").setEphemeral(true).queue();

        if (addReaction) {
            LineReportManager.sendLinesWithReaction(url, event.getChannel());
        } else {
            LineReportManager.sendLinesWithoutReaction(url, event.getChannel());
        }
    }

    private static String getApiKeyword(String type) {
        switch (type) {
            case "accepted":
                return "accepted";
            case "active":
                return "active";
            case "all":
            default:
                return "valid";
        }
    }

    private static String getReadingUrl(String keyword, SlashCommandInteractionEvent event) {
        return "https://voicesofwynn.com/api/unvoiced-line-report/" + keyword + "?npc="
                + event.getOption("npcname").getAsString().replace(" ", "%20") + "&apiKey=" + APIKeys.readingApiKey;
    }
}
