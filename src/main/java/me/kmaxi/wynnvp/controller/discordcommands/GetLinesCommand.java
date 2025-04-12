package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.LineReportHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetLinesCommand implements ICommandImpl {


    @Autowired
    private LineReportHandler lineHandler;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("getlines", "Sends lines based on the specified type in the current channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc", true),
                        new OptionData(OptionType.STRING, "type", "The type of lines to retrieve (accepted, active, all)", false)
                                .addChoice("accepted", LineType.ACCEPTED.name().toLowerCase())
                                .addChoice("active", LineType.ACTIVE.name().toLowerCase())
                                .addChoice("all", LineType.ALL.name().toLowerCase()));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        LineType type = (typeOption != null) ? LineType.valueOf(typeOption.getAsString().toUpperCase()) : LineType.ALL;

        OptionMapping addReactionOption = event.getOption("addreaction");
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();


        event.reply("Sending lines now").setEphemeral(true).queue();


        lineHandler.sendLinesWithoutReaction(type, npcName, event.getChannel());

    }


}