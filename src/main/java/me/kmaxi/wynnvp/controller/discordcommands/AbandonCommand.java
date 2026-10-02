package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AbandonCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("abandon", "Abandon this application thread and notify Cast Managers");
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ANYONE;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Check if the command is used in a thread channel
        if (event.getChannelType() != ChannelType.GUILD_PRIVATE_THREAD &&
            event.getChannelType() != ChannelType.GUILD_PUBLIC_THREAD) {
            event.reply("This command can only be used in application threads.").setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = (ThreadChannel) event.getChannel();

        // Check if the thread is in an application category
        if (!isUnderApplicationCategory(thread)) {
            event.reply("This command can only be used in application threads.").setEphemeral(true).queue();
            return;
        }

        // Verify that the command sender is the thread founder
        String threadName = thread.getName();
        String userName = event.getUser().getName();
        String normalizedUserName = Utils.getChannelName(userName);

        // Thread name format is "npcname-username"
        // Check if the thread name ends with the user's normalized name
        if (!threadName.toLowerCase().endsWith("-" + normalizedUserName.toLowerCase())) {
            event.reply("You can only abandon application threads that you founded.").setEphemeral(true).queue();
            return;
        }

        // Get the guild to find the Cast Manager role
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Unable to process this command.").setEphemeral(true).queue();
            return;
        }

        // Find the Cast Manager role by name
        List<Role> castManagerRoles = guild.getRolesByName("Cast Manager", true);
        String castManagerMention;
        if (!castManagerRoles.isEmpty()) {
            castManagerMention = castManagerRoles.get(0).getAsMention();
        } else {
            // Fallback to just mentioning the role by name if not found
            castManagerMention = "@Cast Manager";
        }

        // Reply to the user first (must be done before removal to avoid errors)
        event.reply("You have abandoned this application thread. A Cast Manager will review it shortly.").setEphemeral(true).queue();

        // Send a message to the thread notifying Cast Managers
        thread.sendMessage(castManagerMention + " The voice actor has abandoned this application thread. " +
                "Please review the thread for anything worth keeping or close it with `/close immediately` if no longer needed.").queue(
                success -> {
                    // Remove the user from the thread after the message is sent
                    thread.removeThreadMember(event.getUser()).queue();
                },
                error -> {
                    // If sending the message fails, still try to remove the user
                    thread.removeThreadMember(event.getUser()).queue();
                }
        );
    }

    private boolean isUnderApplicationCategory(ThreadChannel thread) {
        Long parentCategoryId = thread.getParentChannel().getParentCategoryIdLong();
        return parentCategoryId == Config.APPLY_CATEGORY_ID ||
               parentCategoryId == Config.CATEGORY_ID_2 ||
               parentCategoryId == Config.CLOSING_CATEGORY_ID;
    }
}
