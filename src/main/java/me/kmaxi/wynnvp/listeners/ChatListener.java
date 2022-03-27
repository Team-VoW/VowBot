package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ChatListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Message message = event.getMessage();
        Member member = event.getMember();
        System.out.println("Message recieved from " + event.getAuthor().getName() + ": " + message.getContentDisplay());
        Guild guild;

        try {
            guild = event.getGuild();
        } catch (Exception e) {
            return;
        }
        if (LineReportManager.guild == null) LineReportManager.guild = guild;

        MessageChannel messageChannel = event.getChannel();

        if (event.getAuthor().isBot()) return;

        String[] splitMessage = message.getContentRaw().split(" ");


        Utils.RemoveMutePerms(guild);


        if (splitMessage[0].equalsIgnoreCase("?fixpop")) {
            messageChannel.sendMessage("Fix popping tutorial: https://youtu.be/VmquKL19mJI").queue();
        }

        assert member != null;
        if (!(Utils.isStaff(member))) {
            return;
        }

        switch (splitMessage[0].toLowerCase()) {
            case "?help":
                help(messageChannel);
                return;
            case "?reports-pull":
                LineReportManager.sendAllReports(guild);
                return;
            case "?get-tasks":
                LineReportManager.sendAllAcceptedReports(messageChannel);
                return;
            case "?close":
                closeChannelImmediately(event);
                return;
            case "?closed":
                closeChannelWithCooldown(event);
                return;
            case "?setRole":
                setRole(splitMessage, messageChannel, guild);
                return;
            case "?removeRole":
                openRole(splitMessage, messageChannel, guild);
                return;
            case "?addquest":
                addQuest(splitMessage, messageChannel, guild);
                break;
        }
    }


    private static void closeChannelImmediately(MessageReceivedEvent event) {
       if (event.getTextChannel().getParentCategoryIdLong() != Config.categoryID && event.getTextChannel().getParentCategoryIdLong() != Config.closingCategoryID) {
            return;
        }
        event.getChannel().delete().queue();
    }

    private static void closeChannelWithCooldown(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();

        if (textChannel.getParentCategoryIdLong() != Config.categoryID && textChannel.getParentCategoryIdLong() != Config.closingCategoryID) {
            return;
        }

        try {
            event.getMessage().delete().queue();
            textChannel.sendMessage("Thank you for applying for the role! " +
                    "Sadly, someone else was chosen for it. There are plenty of more chances to come,"
                    + " and we will be glad to evaluate your auditions for any future roles! This application channel will be closed in 24 hours."
                    + "\nIf you want to close it directly say ?close and a staff member will close it").queue();

        } catch (Exception ignored) {
        }
        try {
            textChannel.delete().queueAfter(1, TimeUnit.DAYS);
        } catch (Exception ignored) {
        }
    }

    private void setRole(String[] splitMessage, MessageChannel messageChannel, Guild guild) {
        if (!(splitMessage.length == 4)) {
            messageChannel.sendMessage("Inssuficient arguments. Use ?setRole <QuestName> <NpcName> <PersonWhoGotRole>").queue();
            return;
        }
        setRoleAsTaken(guild, messageChannel, splitMessage[1], splitMessage[2], splitMessage[3]);
    }

    private void openRole(String[] splitMessage, MessageChannel messageChannel, Guild guild) {
        if (!(splitMessage.length == 3)) {
            messageChannel.sendMessage("Inssuficient arguments. Use ?removeRole <QuestName> <NpcName> <PersonWhoGotRole>").queue();
        }
        setRoleAsAvailable(guild, messageChannel, splitMessage[1], splitMessage[2]);
    }

    private static void setRoleAsTaken(Guild guild, MessageChannel messageChannel, String quest, String npcName, String personWhoGotIt) {
        for (Message message : guild.getTextChannelById(Config.channelName).getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
            String messageAsString = message.getContentRaw();
            if (!message.getAuthor().isBot()) {
                continue;
            }
            String[] messageArray = messageAsString.split("\n");
            String questName = messageArray[0].replace("React to apply for a role in ", "");
            questName = questName.replace(">>>", "");
            questName = questName.replace("**", "");
            questName = questName.replace(" ", "");
            if (!questName.equalsIgnoreCase(quest)) {
                continue;
            }
            String out = ">>> **React to apply for a role in " + questName + "**";
            for (int i = 0; i < messageArray.length; i++) {
                String line = messageArray[i];
                if (!line.contains(npcName)) {
                    if (line.contains("to apply for")) {
                        continue;
                    }
                    out += "\n" + line;
                    continue;
                }
                int number = (int) (((double) i / 2.0) + 0.5);
                message.clearReactions(Utils.getUnicode(number)).queue();
                line = line.replace(Utils.convertNumber(number), "x");
                if (line.contains("(")) {
                    String[] split = line.split("\\(");
                    line = split[0];
                }
                line = line.replace(npcName, npcName + " (" + personWhoGotIt + ")");

                out += "\n" + line;
            }
            message.editMessage(out).queue();
            break;
        }
        messageChannel.sendMessage("Assigned role " + npcName + " in " + quest + " quest.").queue();
    }

    private static void setRoleAsAvailable(Guild guild, MessageChannel messageChannel, String quest, String npcName) {
        for (Message message : guild.getTextChannelById(Config.channelName).getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
            String messageAsString = message.getContentRaw();
            if (!message.getAuthor().isBot()) {
                continue;
            }
            String[] messageArray = messageAsString.split("\n");
            String questName = messageArray[0].replace("React to apply for a role in ", "");
            questName = questName.replace(">>>", "");
            questName = questName.replace("**", "");
            questName = questName.replace(" ", "");
            if (!questName.equalsIgnoreCase(quest)) {
                continue;
            }
            String out = ">>> **React to apply for a role in " + questName + "**";
            for (int i = 0; i < messageArray.length; i++) {
                String line = messageArray[i];
                if (!line.contains(npcName)) {
                    if (line.contains("to apply for")) {
                        continue;
                    }
                    out += "\n" + line;
                    continue;
                }
                int number = (int) (((double) i / 2.0) + 0.5);
                message.addReaction(Utils.getUnicode(number)).queue();
                line = ":" + Utils.convertNumber(number) + ": = " + npcName;
                out += "\n" + line;
                break;
            }
            message.editMessage(out).queue();
            break;
        }
        messageChannel.sendMessage("Cleared role " + npcName + " in " + quest + " quest.").queue();
    }

    private static void help(MessageChannel messageChannel) {
        messageChannel.sendMessage("`?close` to close an application."
                + "\n`?setRole <QuestName> <NpcName> <PersonWhoGotRole(CaseSens)>` to assign a role to a person"
                + "\n`?removeRole <QuestName> <NpcName>(CaseSens)>` to remove an assignation"
                + "\n`?addquest <QuestName> <Npc> <Npc>...` to add a new quest. Maximum 9 roles." +
                "\n`?get-tasks` to get all accepted unvoiced lines").queue();
    }

    private static void addQuest(String[] splitMessage, MessageChannel messageChannel, Guild guild) {
        if (splitMessage.length < 3) {
            messageChannel.sendMessage("Inssuficient arguments. Use ?addquest <QuestName> <Npc> <Npc>...").queue();
            return;
        }

        String questName = splitMessage[1];
        ArrayList<String> npcs = new ArrayList<>();
        for (int i = 2; i <= splitMessage.length - 1; i++) {
            npcs.add(splitMessage[i]);
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

        guild.getTextChannelById(Config.channelName).sendMessage(out).queue(message1 -> {
            int index = 1;
            for (String reaction : reactions) {
                message1.addReaction(Utils.getUnicode(index)).queue();
                index++;
            }
        });
    }
}
