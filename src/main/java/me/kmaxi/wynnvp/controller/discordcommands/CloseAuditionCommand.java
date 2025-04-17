package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
        return Commands.slash("close", "Immediately closes an application channel")
                .addSubcommands(new SubcommandData("immediately", "Directly deletes this application channel"))
                .addSubcommands(new SubcommandData("soon", "Closes an application channel in 24h"));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!isUnderApplicationCategory((TextChannel) event.getChannel())) {
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

    private static boolean isUnderApplicationCategory(TextChannel textChannel) {
        return textChannel.getParentCategoryIdLong() == Config.APPLY_CATEGORY_ID || textChannel.getParentCategoryIdLong() == Config.CLOSING_CATEGORY_ID || textChannel.getParentCategoryIdLong() == Config.CATEGORY_ID_2;
    }

    private static void closeChannelImmediately(SlashCommandInteractionEvent event) {

        event.reply("Closing channel").setEphemeral(true).queue();
        event.getChannel().delete().queue();
    }

    private static void closeChannelWithCoolDown(SlashCommandInteractionEvent event) {
        MessageChannelUnion textChannel = event.getChannel();

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
}
