package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Comparator;

import static me.kmaxi.wynnvp.utils.Utils.permissions;
import static me.kmaxi.wynnvp.utils.Utils.traineePerms;

public class AddEmoteListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() == Config.channelName) {
            voiceApplyReact(event);
        } else if ((event.getChannel().getIdLong() == Config.reportedLines
                || event.getChannel().getIdLong() == Config.acceptedLines
                || event.getChannel().getIdLong() == Config.staffBotChat) && Utils.isAdmin(event.getMember())) {
            LineReportManager.lineReportReact(event);
        }

    }


    private void voiceApplyReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        int numberReacted = Utils.whichNumberWasReacted(event.getEmoji().getName());

        String[] messageArray = message.split("\n");

        String line = messageArray[(2 * numberReacted) - 1];
        String[] splitLine = line.split("=");

        String npcName = splitLine[1].replace(" ", "");

        Guild guild = event.getGuild();
        String playerNameNoFormat = event.getMember().getEffectiveName();
        Utils.formatName(playerNameNoFormat, guild, playerName -> {
            guild.getCategoryById(Config.spamCategoryID).getChannels().forEach(guildChannel -> {
                guildChannel.delete().queue();
            });
            String channelName = npcName + "-" + playerName;

            if (playersHasTwoApplications(guild, playerName, event.getMember())) {
                int limit = ChannelLimit(guild, event.getMember());
                Utils.sendPrivateMessage(event.getUser(), "You may only have " + limit + " applications open at once. \n If you want to close a current application type `?close` in the application channel. "
                        + "If you want to have more then two audition channels and support the mod even more then feel free to become a Patron:  https://www.patreon.com/Voices_Of_Wynn");
                return;
            }
            if (channelExists(channelName, guild)) {
                Utils.sendPrivateMessage(event.getUser(), "You already have an application for " + npcName + " running. Type `?close` in the application channel to close it");
                return;
            }

            if (applicationCategoryHas50Channels(guild)) {
                Utils.sendPrivateMessage(event.getUser(), "Sorry, there are already 50 applications going on right now which is the limit of discord. Message a staff member and they will clean up some channels");
            }

            String questName = messageArray[0].replace("React to apply for a role in", "");
            questName = questName.replace(">>>", "");
            String finalQuestName = questName;
            guild.createTextChannel(channelName, guild.getCategoryById(Config.categoryID))
                    .setTopic(event.getMember().getEffectiveName() + "s " + npcName + " application")
                    .addMemberPermissionOverride(event.getMember().getIdLong(), permissions(), null)
                    .addRolePermissionOverride(Config.roleID, permissions(), null)
                    .addRolePermissionOverride(820690089427861535l, null, permissions())
                    .addRolePermissionOverride(Config.traineeRole, traineePerms(), null)
                    .addPermissionOverride(guild.getPublicRole(), null, permissions())
                    .queue(textChannel -> {
                        textChannel.sendMessage("Thank you " + event.getUser().getAsMention() + " for applying for **" + npcName + "** in" + finalQuestName + "."
                                + "\n \nPlease go to " + guild.getGuildChannelById(Config.scriptChannelID).getAsMention() + " and find the script there. Download it and record yourself saying **the first three sentences** that " + npcName + " says in that quest. "
                                + "Record yourself saying those with a recording program software such as audacity or this webpage https://vocaroo.com/ and send it in this channel "
                                + "and we will tell you you if you fit role."
                                + "\n\n**Make sure** that you have read " + guild.getGuildChannelById(823509081498451988L).getAsMention() + " before applying. There are many useful tricks there and some must does"
                                + "\n\nTo delete this application simply say `?close`, this will not close your application but will prompt staff to close it."
                                + "\n\nBy opening this application, you've agreed to the terms listed in " + guild.getGuildChannelById(820027818799792129L).getAsMention()).queue();
                        sortChannels(guild);
                    });
        });
    }

    private boolean playersHasTwoApplications(Guild guild, String name, Member member) {
        name = name.toLowerCase();
        int counter = 0;
        int limit = ChannelLimit(guild, member);


        for (GuildChannel guildChannel : guild.getCategoryById(Config.categoryID).getChannels()) {
            if (guildChannel.getName().contains(name)) {
                counter++;
            }

            if (counter == limit) {
                return true;
            }
        }
        return false;
    }

    private int ChannelLimit(Guild guild, Member member) {
        int limit = 2;
        if (member.getRoles().contains(guild.getRoleById(Config.donatorPlusRole))) limit = 3;
        if (member.getRoles().contains(guild.getRoleById(Config.vipRole))) limit = 4;
        return limit;
    }


    private boolean channelExists(String channelname, Guild guild) {
        for (TextChannel textChannel : guild.getTextChannels()) {
            if (textChannel.getName().equalsIgnoreCase(channelname)) {
                return true;
            }
        }
        return false;
    }

    private void sortChannels(Guild guild) {
        if (guild.getCategoryById(Config.categoryID).getChannels().isEmpty()) {
            System.out.println("Tried to sort empty category.");
            return;
        }
        guild.getCategoryById(Config.categoryID)
                .modifyTextChannelPositions()
                .sortOrder(new Comparator<GuildChannel>() {
                    @Override
                    public int compare(GuildChannel o1, GuildChannel o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                }).queue();
    }

    private boolean applicationCategoryHas50Channels(Guild guild) {
        return guild.getCategoryById(Config.categoryID).getChannels().size() >= 50;
    }


}


