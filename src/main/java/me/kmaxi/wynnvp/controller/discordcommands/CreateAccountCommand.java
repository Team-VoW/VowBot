package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.data.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CreateAccountCommand implements ICommandImpl {

    private final UserService userService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("createaccount", "Upgrades this users role here and on website and creates an account if they don't have one.")
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

        User user = taggedUser.getAsUser();
        Member member = Objects.requireNonNull(event.getGuild()).getMember(user);

        if (member == null) {
            event.getHook().setEphemeral(true).editOriginal("ERROR! COULD NOT FIND THE USER!!").queue();
            return;
        }

        try {
            String password = userService.createAccount(member);

            if (password.isEmpty()) {
                event.getHook().setEphemeral(true).editOriginal("This person already has an account.").queue();
            } else {
                user.openPrivateChannel().queue((channel) ->
                        channel.sendMessage("An account with the name " + member.getUser().getName() + " and the temporary password ||" +
                                password + "|| has been created for you on our website https://voicesofwynn.com/ " +
                                "\n\n Feel free to go in there and change your bio, profile picture and more! :grin:").queue(
                                success -> event.getHook().setEphemeral(true).editOriginal("Account created and private message sent to the user.").queue(),
                                error -> event.getHook().setEphemeral(true).editOriginal("Failed to send a message to the user. Please send them the password: ||" + password + "||").queue()));
            }
        } catch (IOException e) {
            event.getHook().setEphemeral(true).editOriginal("Failed to create account: " + e.getMessage()).queue();
            throw new RuntimeException(e);
        }
    }
}
