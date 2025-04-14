package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.audition.AuditionsHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FinishedRoleCommand implements ICommandImpl {

    @Autowired
    private AuditionsHandler auditionsHandler;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("finishedrole", "Upgrades this users role here and on website and creates an account if they don't have one.")
                .addOptions(new OptionData(OptionType.USER, "user", "The voice actors discord", true));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        OptionMapping taggedUser = event.getOption("user");

        if (taggedUser == null) {
            event.getHook().setEphemeral(true).editOriginal("ERROR! NO USER TAGGED!!").queue();
            return;
        }

        Member member = Objects.requireNonNull(event.getGuild()).getMember(taggedUser.getAsUser());

        if (member == null) {
            event.getHook().setEphemeral(true).editOriginal("ERROR! COULD NOT FIND THE USER!!").queue();
            return;
        }
        auditionsHandler.finishedRole(member, event.getGuild()).thenAccept(message -> {
            // Place your code here to handle the message once it finishes
            event.getHook().setEphemeral(false).editOriginal(message).queue();
        });
    }
}
