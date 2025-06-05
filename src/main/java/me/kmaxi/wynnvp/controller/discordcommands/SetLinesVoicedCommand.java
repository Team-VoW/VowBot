package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SetLinesVoicedCommand implements ICommandImpl {

    private final LineReportHandler lineHandler;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("setlinesvoiced", "Sets all lines in the json file to voiced")
                .addOptions(
                        new OptionData(OptionType.ATTACHMENT, "file", "The exact name of the npc", true));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("file");
        if (typeOption == null) {
            event.reply("Please provide a file").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue(hook -> {
            String result = lineHandler.setLinesAsVoiced(typeOption.getAsAttachment());
            hook.editOriginal(result).queue();
        });
    }


}