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
                        new OptionData(OptionType.STRING, "questname", "The name of the quest", true),
                        new OptionData(OptionType.STRING, "npcA", "The first's npc name", true),
                        new OptionData(OptionType.STRING, "npcB", "The second's npc name", false),
                        new OptionData(OptionType.STRING, "npcC", "The third's npc name", false),
                        new OptionData(OptionType.STRING, "npcD", "The fourth's npc name", false),
                        new OptionData(OptionType.STRING, "npcE", "The fifth's npc name", false),
                        new OptionData(OptionType.STRING, "npcF", "The sixth's npc name", false),
                        new OptionData(OptionType.STRING, "npcG", "The seventh's npc name", false),
                        new OptionData(OptionType.STRING, "npcH", "The eight's npc name", false),
                        new OptionData(OptionType.STRING, "npcI", "The ninth's npc name", false),
                        new OptionData(OptionType.STRING, "npcJ", "The tenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcK", "The eleventh's npc name", false),
                        new OptionData(OptionType.STRING, "npcL", "The twelveth's npc name", false),
                        new OptionData(OptionType.STRING, "npcM", "The thirteenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcN", "The fourteenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcO", "The fifteenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcP", "The sixteenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcQ", "The seventeenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcR", "The eightheenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcS", "The nineteenth's npc name", false),
                        new OptionData(OptionType.STRING, "npcT", "The twentieth's npc name", false),
                        new OptionData(OptionType.STRING, "npcU", "The twentyfirst's npc name", false),
                        new OptionData(OptionType.STRING, "npcV", "The twentysecond's npc name", false),
                        new OptionData(OptionType.STRING, "npcW", "The twentythird's npc name", false),
                        new OptionData(OptionType.STRING, "npcX", "The twentyfourth's npc name", false),
                        new OptionData(OptionType.STRING, "npcY", "The twentyfifth's npc name", false),
                        new OptionData(OptionType.STRING, "npcZ", "The twentysixth's npc name", false),
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
            textChannel = channelOption.getAsChannel().asStandardGuildMessageChannel();

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
