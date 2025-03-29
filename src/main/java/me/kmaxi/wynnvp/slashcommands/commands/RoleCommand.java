package me.kmaxi.wynnvp.slashcommands.commands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.interfaces.StringIntInterface;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Objects;

import static me.kmaxi.wynnvp.WynnVPBotMain.guild;

public class RoleCommand implements ICommandImpl {

    private final String openSubCommand = "open";
    private final String setSubCommand = "set";
    @Override
    public CommandData getCommandData() {

        return Commands.slash("role", "Handles setting of roles of castings.")
                .addSubcommands(new SubcommandData(openSubCommand, "Sets an already set role for castings")
                        .addOptions(
                                new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                                new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true)))
                .addSubcommands(new SubcommandData(setSubCommand, "Sets a role that is open in the casting")
                        .addOptions(
                                new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                                new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true),
                                new OptionData(OptionType.USER, "user", "The person that you want to cast for this role", true)));

    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case openSubCommand:
                setRoleAsAvailable(event);
                break;
            case setSubCommand:
                setRoleAsTaken(event);
        }
    }

    private  void setRoleAsTaken(SlashCommandInteractionEvent event) {

        IMentionable personWhoGotIt = Objects.requireNonNull(event.getOption("user")).getAsMentionable();
        String questName = Objects.requireNonNull(event.getOption("questname")).getAsString();
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            event.reply("Could not find quest " + questName + " or npc name " + npcName).setEphemeral(true).queue();
            return;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((number, line) -> {
            message.clearReactions(Emoji.fromUnicode(Utils.getUnicode(number))).queue();
            line = line.replace(Utils.convertNumber(number), "x");
            if (line.contains("(")) {
                String[] split = line.split("\\(");
                line = split[0];
            }
            return line.replace(npcName, npcName + " (" + personWhoGotIt.getAsMention() + ")");
        }));

        event.reply("Assigned role " + npcName + " in " + questName + " quest.").setEphemeral(true).queue();

    }

    private void setRoleAsAvailable(SlashCommandInteractionEvent event) {

        String questName = Objects.requireNonNull(event.getOption("questname")).getAsString();
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            event.reply("Could not find quest " + questName + " or npc name " + npcName).setEphemeral(true).queue();
            return;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((lineNumber, lineBefore) -> {
            message.addReaction(Emoji.fromUnicode(Utils.getUnicode(lineNumber))).queue();
            return ":" + Utils.convertNumber(lineNumber) + ": = " + npcName;
        }));

        event.reply("Cleared role " + npcName + " in " + questName + " quest.").setEphemeral(true).queue();
    }


    private Message getCastingMessage(String quest, String npcName) {
        for (Message message : Objects.requireNonNull(guild.getNewsChannelById(Config.channelName)).getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
            String messageAsString = message.getContentRaw();
            if (!message.getAuthor().isBot()) {
                continue;
            }
            String[] messageArray = messageAsString.split("\n");
            String questName = messageArray[0].replace("React to apply for a role in ", "");
            questName = questName.replace(">>>", "");
            questName = questName.replace("**", "");
            questName = questName.replace(" ", "");
            if (!questName.equalsIgnoreCase(quest) || !messageAsString.toLowerCase().contains(npcName)) {
                continue;
            }
            return message;
        }
        return null;
    }

    private void replaceLineWhereNpcIs(Message message, String npcName, String questName, StringIntInterface lineChange) {
        String[] messageArray = message.getContentRaw().split("\n");
        StringBuilder out = new StringBuilder(">>> **React to apply for a role in " + questName + "**");

        boolean hasChangedAline = false;
        for (int i = 0; i < messageArray.length; i++) {
            String line = messageArray[i];
            if (!line.contains(npcName) || hasChangedAline) {
                if (line.contains("to apply for")) {
                    continue;
                }
                out.append("\n").append(line);
                continue;
            }
            int number = (int) (((double) i / 2.0) + 0.5);
            message.addReaction(Emoji.fromUnicode(Utils.getUnicode(number))).queue();
            line = lineChange.operation((int) (((double) i / 2.0) + 0.5), line);
            out.append("\n").append(line);
            hasChangedAline = true;
        }
        message.editMessage(out.toString()).queue();
    }


}
