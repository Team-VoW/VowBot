package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.ApiService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class FinishedRoleCommand implements ICommandImpl {

    @Autowired
    private ApiService apiService;

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

        CompletableFuture<Void> completableFuture = Utils.upgradeActorRole(member, event.getGuild());

        //Role was not upgraded because person is already at highest role
        if (completableFuture == null) {
            event.getHook().setEphemeral(false).editOriginal("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                    "\nBecause you already are expert actor your role stayed the same this time :grin:").queue();
            return;
        }

        completableFuture.thenRunAsync(() -> {
            // Role added successfully, wait for 1 second and then check the member's roles because it doesn't update directly
            Objects.requireNonNull(event.getGuild()).retrieveMemberById(member.getId()).queueAfter(1, TimeUnit.SECONDS, updatedMember -> {
                try {
                    String password = apiService.createAccount(member);

                    if (password.isEmpty()) {
                        event.getHook().setEphemeral(false).editOriginal("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                                "\nYour actor role has been upgraded here and on the Website :partying_face:").queue();
                    } else {
                        event.getHook().setEphemeral(false).editOriginal("Thanks a lot for voicing your very first character for us " + member.getAsMention() + ":heart::partying_face:." +
                                "\n\n An account with the name " + member.getUser().getName() + " and the temporary password ||" +
                                password + "|| has been created for you on our website https://voicesofwynn.com/ " +
                                "\n\n Once everyone voice actor from this quest has sent in their lines, everything will be " +
                                " added to all the voice actors accounts. Feel free to go in there and change your bio, profile picture and more! :grin:").queue();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
