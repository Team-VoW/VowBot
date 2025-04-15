package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.audition.AuditionsHandler;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenCastingCommand implements ICommandImpl {

    private final AuditionsHandler auditionsHandler;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("opencasting", "Opens casting for a new quest")
                .addOptions(
                        new OptionData(OptionType.STRING, "questname", "The name of the quest with no spaces. Use - between words", true),
                        new OptionData(OptionType.STRING, "npc1", "The firsts npc name", true),
                        new OptionData(OptionType.STRING, "npc2", "The seconds npc name", false),
                        new OptionData(OptionType.STRING, "npc3", "The thirds npc name", false),
                        new OptionData(OptionType.STRING, "npc4", "The fourths npc name", false),
                        new OptionData(OptionType.STRING, "npc5", "The fifths npc name", false),
                        new OptionData(OptionType.STRING, "npc6", "The sixths npc name", false),
                        new OptionData(OptionType.STRING, "npc7", "The sevenths npc name", false),
                        new OptionData(OptionType.STRING, "npc8", "The eights npc name", false),
                        new OptionData(OptionType.STRING, "npc9", "The ninths npc name", false),
                        new OptionData(OptionType.CHANNEL, "channel", "The channel to send the poll to", false)
                                .setChannelTypes(ChannelType.NEWS, ChannelType.TEXT));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        List<OptionMapping> options = event.getOptions();
        String questName = options.get(0).getAsString();

        if (questName.contains(" ")) {
            event.reply("Quest name cannot contain spaces. Use - instead").setEphemeral(true).queue();
            return;
        }

        ArrayList<String> npcs = new ArrayList<>();
        for (int i = 1; i < options.size(); i++) {
            npcs.add(options.get(i).getAsString());
        }

        // Get the channel option
        OptionMapping channelOption = event.getOption("channel");
        // Check if the channel option is provided
        MessageChannel textChannel;
        if (channelOption != null) {
            // Get the channel by ID
            textChannel = channelOption.getAsChannel().asTextChannel();

            //Remove last npc since it is the channel id
            npcs.remove(npcs.size() - 1);
        } else {
            // If no channel is provided, use the current channel
            textChannel = event.getChannel();
        }

        auditionsHandler.setupPoll(questName, npcs, textChannel);

        event.reply("Successfully created the " + questName + " quest with " + npcs.size() + " npcs.").setEphemeral(true).queue();
    }
}
