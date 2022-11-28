package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.Utils;
import me.kmaxi.wynnvp.interfaces.StringIntInterface;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static me.kmaxi.wynnvp.WynnVPBotMain.guild;

public class ApplicationCommands {

    public static void close(SlashCommandInteractionEvent event) {

        if (!isUnderApplicationCategory(event.getTextChannel())) {
            event.reply("Can only do this command in application channels.").setEphemeral(true).queue();
            return;
        }

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "immediately":
                closeChannelImmediately(event);
                break;
            case "soon":
                closeChannelWithCoolDown(event);
        }
    }

    private static void closeChannelImmediately(SlashCommandInteractionEvent event) {

        event.reply("Closing channel").setEphemeral(true).queue();
        event.getChannel().delete().queue();
    }

    private static void closeChannelWithCoolDown(SlashCommandInteractionEvent event) {
        TextChannel textChannel = event.getTextChannel();

        try {
            textChannel.sendMessage("Thank you for applying for the role! " +
                    "Sadly, someone else was chosen for it. There are plenty of more chances to come,"
                    + " and we will be glad to evaluate your auditions for any future roles! This application channel will be closed in 24 hours."
                    + "\nIf you want to close it directly say ?close and a staff member will close it").queue();

            event.reply("Channel will be deleted in 24h.").setEphemeral(true).queue();

            textChannel.delete().queueAfter(1, TimeUnit.DAYS);

        } catch (Exception ignored) {
        }
    }

    private static boolean isUnderApplicationCategory(TextChannel textChannel) {
        return textChannel.getParentCategoryIdLong() == Config.categoryID || textChannel.getParentCategoryIdLong() == Config.closingCategoryID;
    }


    public static void setRoleAsTaken(SlashCommandInteractionEvent event) {

        IMentionable personWhoGotIt = Objects.requireNonNull(event.getOption("user")).getAsMentionable();
        String questName = Objects.requireNonNull(event.getOption("questname")).getAsString();
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            event.reply("Could not find quest " + questName + " or npc name " + npcName).setEphemeral(true).queue();
            return;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((number, line) -> {
            message.clearReactions(Utils.getUnicode(number)).queue();
            line = line.replace(Utils.convertNumber(number), "x");
            if (line.contains("(")) {
                String[] split = line.split("\\(");
                line = split[0];
            }
            return line.replace(npcName, npcName + " (" + personWhoGotIt.getAsMention() + ")");
        }));

        event.reply("Assigned role " + npcName + " in " + questName + " quest.").setEphemeral(true).queue();

    }

    public static void setRoleAsAvailable(SlashCommandInteractionEvent event) {

        String questName = Objects.requireNonNull(event.getOption("questname")).getAsString();
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            event.reply("Could not find quest " + questName + " or npc name " + npcName).setEphemeral(true).queue();
            return;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((lineNumber, lineBefore) -> {
            message.addReaction(Utils.getUnicode(lineNumber)).queue();
            return ":" + Utils.convertNumber(lineNumber) + ": = " + npcName;
        }));

        event.reply("Cleared role " + npcName + " in " + questName + " quest.").setEphemeral(true).queue();
    }


    private static Message getCastingMessage(String quest, String npcName) {
        for (Message message : Objects.requireNonNull(guild.getTextChannelById(Config.channelName)).getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
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

    private static void replaceLineWhereNpcIs(Message message, String npcName, String questName, StringIntInterface lineChange) {
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
            message.addReaction(Utils.getUnicode(number)).queue();
            line = lineChange.operation((int) (((double) i / 2.0) + 0.5), line);
            out.append("\n").append(line);
            hasChangedAline = true;
        }
        message.editMessage(out.toString()).queue();
    }


    public static void addQuest(SlashCommandInteractionEvent event) {
        List<OptionMapping> options = event.getOptions();
        String questName = options.get(0).getAsString();


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

        event.getGuild().getTextChannelById(Config.channelName).sendMessage(out).queue(message1 -> {
            int index = 1;
            for (String reaction : reactions) {
                message1.addReaction(Utils.getUnicode(index)).queue();
                index++;
            }
        });

        event.reply("Succefully created the " + questName + " quest with " + npcs.size() + " npcs.").setEphemeral(true).queue();
    }
}
