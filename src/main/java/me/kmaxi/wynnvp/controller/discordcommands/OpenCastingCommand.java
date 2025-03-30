package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
public class OpenCastingCommand implements ICommandImpl {
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
                        new OptionData(OptionType.STRING, "npc9", "The ninths npc name", false));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        List<OptionMapping> options = event.getOptions();
        String questName = options.get(0).getAsString();

        if (questName.contains(" ")){
            event.reply("Quest name cannot contain spaces. Use - instead").setEphemeral(true).queue();
            return;
        }


        ArrayList<String> npcs = new ArrayList<>();
        for (int i = 1; i < options.size(); i++) {
            npcs.add(options.get(i).getAsString());
        }

        ArrayList<String> reactions = new ArrayList<>();
        String out = ">>> **React to apply for a role in " + questName + "**";
        int i = 1;
        for (String npc : npcs) {
            if (i == 10) {
                break;
            }
            out += "\n:" + Utils.convertNumber(i) + ": = " + npc + "\n";
            reactions.add(String.valueOf(i));
            i++;
        }

        NewsChannel textChannel = event.getGuild().getNewsChannelById(Config.channelName);
        System.out.println(textChannel);
        if (textChannel == null) {
            event.reply("The channel with ID " + Config.channelName + " could not be found. Please check the channel ID and bot permissions.").setEphemeral(true).queue();
            return;
        }

        textChannel.sendMessage(out).queue(message1 -> {
            int index = 1;
            for (String reaction : reactions) {
                message1.addReaction(Emoji.fromUnicode(Utils.getUnicode(index))).queue();
                index++;
            }
        });

        event.reply("Successfully created the " + questName + " quest with " + npcs.size() + " npcs.").setEphemeral(true).queue();
    }
}
