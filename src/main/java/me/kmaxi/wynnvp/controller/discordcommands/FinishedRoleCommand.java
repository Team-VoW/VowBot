package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.kmaxi.wynnvp.utils.APIUtils.updateUserDataOnWebsite;
@Component
public class FinishedRoleCommand implements ICommandImpl {
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

        Member member = event.getGuild().getMember(taggedUser.getAsUser());

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
                String postArguments = "";

                postArguments = addPostArgument(postArguments, "discordName=" + member.getUser().getAsTag());

                postArguments = addPostArgument(postArguments, "discordId=" + member.getUser().getId());

                postArguments = addPostArgument(postArguments, "roles=" + getRolesArguments(member));

                try {
                    String password = updateUserDataOnWebsite(postArguments);

                    //No new account was created.
                    if (password.equals("")) {

                        event.getHook().setEphemeral(false).editOriginal("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                                "\nYour actor role has been upgraded here and on the Website :partying_face:").queue();
                        return;
                    }

                    password = extractPassword(password);

                    //As this is a new account we send another post request with the profile pic
                    postArguments = appendProfilePictureURL(postArguments, null, member.getUser());
                    updateUserDataOnWebsite(postArguments);

                    event.getHook().setEphemeral(false).editOriginal("Thanks a lot for voicing your very first character for us " + member.getAsMention() + ":heart::partying_face:." +
                            "\n\n An account with the name " + member.getUser().getName() + " and the temporary password ||" +
                            password + "|| has been created for you on our website https://voicesofwynn.com/ " +
                            "" +
                            "\n\n Once everyone voice actor from this quest has sent in their lines, everything will be " +
                            " added to all the voice actors accounts. Feel free to go in there and change your bio, profile picture and more! :grin:").queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }


    private String extractPassword(String input) {

        Pattern pattern = Pattern.compile("\"tempPassword\":\"([a-zA-Z\\d]+)\"");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "ERROR! COULD NOT FIND PASSWORD!";
    }


    private String appendProfilePictureURL(String postArguments, JSONObject userInfo, User discordMember) {

        String profilePictureURL = userInfo == null ? "default.png" : userInfo.getString("avatarLink");
        if (profilePictureURL.equals("default.png") || profilePictureURL.equals("dynamic/avatars/default.png")) {
            String addition = "imgurl=" + discordMember.getEffectiveAvatarUrl();

            if (addition.contains("null"))
                return postArguments;

            return addPostArgument(postArguments, addition);
        }
        return postArguments;
    }


    private String getRolesArguments(Member discordMember) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        //For each role user has in discord
        discordMember.getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.discordRolesToUpdateToWebsite.contains(roleName)) {
                //Return functions as a continue statement in a .forEach loop
                return;
            }

            stringBuilder.append("\"").append(roleName).append("\",");

        });

        //Removes the last comma
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }


    private String addPostArgument(String currentArguments, String addition) {
        if (currentArguments.contains("?")) {
            return currentArguments + "&" + addition;
        } else
            return currentArguments + "?" + addition;
    }
}
