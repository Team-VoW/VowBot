package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.DiscordPollHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * Ends a Discord casting and sends its auditions to the website's casting page, where staff vote.
 * Casting Call Club castings are imported on the website itself, so the old url option only points there.
 */
@Slf4j
@Component
public class SetupPollCommand implements ICommandImpl {

    private static final String CASTING_ADMIN_URL = "https://app.voicesofwynn.com/admin/casting";

    private final DiscordPollHandler pollHandler;
    private final Executor pollSetupExecutor;

    public SetupPollCommand(DiscordPollHandler pollHandler, @Qualifier("pollSetupExecutor") Executor pollSetupExecutor) {
        this.pollHandler = pollHandler;
        this.pollSetupExecutor = pollSetupExecutor;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("setuppoll", "Sends a Discord casting's auditions to the website for voting")
                .addOptions(new OptionData(OptionType.STRING, "quest", "The quest name of the Discord casting", false))
                .addOptions(new OptionData(OptionType.STRING, "url", "Casting Call Club castings are now imported on the website", false));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        log.info("Set up poll command executed by {}", event.getUser().getName());
        event.deferReply().setEphemeral(true).queue();

        OptionMapping questName = event.getOption("quest");
        if (questName != null) {
            String quest = questName.getAsString();
            // Downloading and uploading every audition takes a while; keep the gateway thread free.
            pollSetupExecutor.execute(() -> {
                try {
                    event.getHook().editOriginal(pollHandler.setupPoll(quest)).queue();
                } catch (RuntimeException e) {
                    log.error("Unexpected error while setting up the casting for {}", quest, e);
                    event.getHook().editOriginal("An error occurred: " + e.getMessage()).queue();
                }
            });
            return;
        }

        if (event.getOption("url") != null) {
            event.getHook().editOriginal("Casting Call Club castings are imported on the website now: create a round at "
                    + CASTING_ADMIN_URL + " and paste the casting call link into \"Import from Casting Call Club\".").queue();
            return;
        }

        event.getHook().editOriginal("Please provide the quest name of the Discord casting.").queue();
    }
}
