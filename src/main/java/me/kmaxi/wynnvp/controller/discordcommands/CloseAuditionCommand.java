package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class CloseAuditionCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("close", "Closes an application channel or thread")
                .addSubcommands(new SubcommandData("immediately", "Directly closes this application channel/thread"))
                .addSubcommands(new SubcommandData("soon", "Closes an application channel/thread in 24h"));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        ChannelType channelType = event.getChannelType();
        boolean isThread = channelType == ChannelType.GUILD_PRIVATE_THREAD ||
                          channelType == ChannelType.GUILD_PUBLIC_THREAD;
        boolean isTextChannel = channelType == ChannelType.TEXT;

        if (!isTextChannel && !isThread) {
            event.reply("Can only do this command in application channels or threads.").setEphemeral(true).queue();
            return;
        }

        // Validate it's under an application category
        if (isTextChannel) {
            TextChannel textChannel = (TextChannel) event.getChannel();
            if (!isUnderApplicationCategory(textChannel)) {
                event.reply("Can only do this command in application channels.").setEphemeral(true).queue();
                return;
            }
        } else if (isThread) {
            ThreadChannel threadChannel = (ThreadChannel) event.getChannel();
            if (!isUnderApplicationCategory(threadChannel)) {
                event.reply("Can only do this command in application threads.").setEphemeral(true).queue();
                return;
            }
        }

        String subCommandName = Objects.requireNonNull(event.getSubcommandName());

        if (subCommandName.equals("immediately")) {
            if (isThread) {
                closeThreadImmediately(event);
            } else {
                closeChannelImmediately(event);
            }
        } else if (subCommandName.equals("soon")) {
            if (isThread) {
                closeThreadWithCoolDown(event);
            } else {
                closeChannelWithCoolDown(event);
            }
        }
    }

    private static boolean isUnderApplicationCategory(TextChannel textChannel) {
        return textChannel.getParentCategoryIdLong() == Config.APPLY_CATEGORY_ID ||
               textChannel.getParentCategoryIdLong() == Config.CLOSING_CATEGORY_ID ||
               textChannel.getParentCategoryIdLong() == Config.CATEGORY_ID_2;
    }

    private static boolean isUnderApplicationCategory(ThreadChannel threadChannel) {
        Long parentCategoryId = threadChannel.getParentChannel().getParentCategoryIdLong();
        return parentCategoryId == Config.APPLY_CATEGORY_ID ||
               parentCategoryId == Config.CLOSING_CATEGORY_ID ||
               parentCategoryId == Config.CATEGORY_ID_2;
    }

    private static void closeChannelImmediately(SlashCommandInteractionEvent event) {

        event.reply("Closing channel").setEphemeral(true).queue();
        event.getChannel().delete().queue();
    }

    private static void closeChannelWithCoolDown(SlashCommandInteractionEvent event) {
        MessageChannelUnion textChannel = event.getChannel();

        textChannel.sendMessage("Thank you for applying for the role! " +
                "Sadly, someone else was chosen for it. There are plenty of more chances to come,"
                + " and we will be glad to evaluate your auditions for any future roles! This application channel will be closed in 24 hours."
                + "\nIf you want to close it directly say ?close and a staff member will close it").queue();

        event.reply("Channel will be deleted in 24h.").setEphemeral(true).queue();

        textChannel.delete().queueAfter(1, TimeUnit.DAYS);
    }

    private static void closeThreadImmediately(SlashCommandInteractionEvent event) {
        ThreadChannel thread = (ThreadChannel) event.getChannel();

        event.reply("Closing thread").setEphemeral(true).queue();

        // Lock and rename the thread instead of deleting (threads don't count toward limits)
        String currentName = thread.getName();
        String newName = currentName.startsWith("❌") ? currentName : "❌" + currentName;

        thread.getManager()
                .setName(newName)
                .setLocked(true)
                .setArchived(true)
                .queue();
    }

    private static void closeThreadWithCoolDown(SlashCommandInteractionEvent event) {
        ThreadChannel thread = (ThreadChannel) event.getChannel();

        thread.sendMessage("Thank you for applying for the role! " +
                "Sadly, someone else was chosen for it. There are plenty of more chances to come,"
                + " and we will be glad to evaluate your auditions for any future roles! This application thread will be closed in 24 hours."
                + "\nIf you want to close it directly, a staff member can use `/close immediately`").queue();

        event.reply("Thread will be closed in 24h.").setEphemeral(true).queue();

        // Schedule thread to be locked and renamed after 24 hours
        thread.getManager()
                .setName("❌" + thread.getName())
                .setLocked(true)
                .setArchived(true)
                .queueAfter(1, TimeUnit.DAYS);
    }
}
