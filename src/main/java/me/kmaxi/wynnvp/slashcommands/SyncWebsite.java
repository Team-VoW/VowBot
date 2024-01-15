package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.WynnVPBotMain;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.kmaxi.wynnvp.utils.APIUtils.getJsonData;
import static me.kmaxi.wynnvp.utils.APIUtils.updateUserDataOnWebsite;

public class SyncWebsite {

    //private static HashMap<String, Member> usernameMember;

    /**
     * @param action "getAllUsers" returns all
     * @return Returns the users' data from the website
     */
    private static JSONArray getUsersData(String action) {

        try {
            return getJsonData(Config.URL_DiscordIntegration + "?action=" + action + "&apiKey=" + APIKeys.discordIntegrationAPIKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void SyncAllUsers(SlashCommandInteractionEvent event) {

        JSONArray userDataArray = getUsersData("getAllUsers");


        event.deferReply().queue();

        //For each website account
        for (int i = 0; i < userDataArray.length(); i++) {
            JSONObject userInfo = userDataArray.getJSONObject(i);

            SyncUser(userInfo);
        }

        event.getHook().setEphemeral(true).editOriginal("Synced all users").queue();
    }

    public static void FinishedRole(SlashCommandInteractionEvent event) {

        event.deferReply().queue();


        OptionMapping taggedUser = event.getOption("user");

        Member member = taggedUser == null ? Utils.getFirstMemberWithSpecialPermission(event.getGuildChannel()) : event.getGuild().getMember(taggedUser.getAsUser());

        if (member == null) {
            event.getHook().setEphemeral(true).editOriginal("ERROR! COULD NOT FIND ANY USER THAT THIS CHANNEL WAS MADE FOR!").queue();
            return;
        }


        CompletableFuture<Void> completableFuture = Utils.upgradeActorRole(member, event.getGuild());

        //Role was not upgraded because person is already at highest role
        if (completableFuture == null){
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

           //     event.getChannel().sendMessage("Post arguments: " + postArguments).queue();

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


    private static String extractPassword(String input) {

        Pattern pattern = Pattern.compile("\"tempPassword\":\"([a-zA-Z\\d]+)\"");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "ERROR! COULD NOT FIND PASSWORD!";
    }


    /**
     * Syncs website users if they are in the discord
     *
     * @param userInfo The Json object of the website user
     */
    private static void SyncUser(JSONObject userInfo) {
        String discordUserName = "";

        //For some reason a regular null check does not work, so we just check the string value
        if (!userInfo.get("discordName").toString().equals("null")) {
            discordUserName = userInfo.getString("discordName");
        }

        long uuidOnWebsite = 0;

        //For some reason a regular null check does not work, so we just check the string value
        if (!userInfo.get("discordId").toString().equals("null")) {
            uuidOnWebsite = userInfo.getLong("discordId");
        }

        Member member = getDiscordMember(uuidOnWebsite, discordUserName);


        if (member == null) {
            System.out.println("User " + userInfo.getString("displayName") + " with discord: " + discordUserName + " is not in the discord");
            return;
        }
        User user = member.getUser();

        String postArguments = "";

        postArguments = appendDiscordUUID(postArguments, uuidOnWebsite, user);

        postArguments = appendProfilePictureURL(postArguments, userInfo, user);

        postArguments = appendRolesL(postArguments, userInfo, member);


        if (postArguments.equals(""))
            return;

        //We always add the discord name and id so that the website can find the correct user.
        if (!postArguments.contains("discordName="))
            postArguments = addPostArgument(postArguments, "discordName=" + user.getAsTag());
        if (!postArguments.contains("discordId="))
            postArguments = addPostArgument(postArguments, "discordId=" + user.getId());


        try {
            updateUserDataOnWebsite(postArguments);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the discord member from either UUID, if a player with that exists, if no player with
     * the uuid exists then it tries getting the member via the username
     *
     * @param uuid            The uuid of the member
     * @param discordUserName The discord name of the member, only used if no person with the UUID was found
     * @return returns the discord member
     */
    private static Member getDiscordMember(long uuid, String discordUserName) {
        Guild guild = WynnVPBotMain.guild;

        Member member = null;

        if (uuid != 0) {
            return guild.getMemberById(uuid);
        }


        if (discordUserName.equals(""))
            return null;

        if (discordUserName.contains("#")){
            return guild.getMemberByTag(discordUserName);
        }


        return null;

    }

    private static String appendDiscordUUID(String postArguments, long uuidOnWebsite, User discordMember) {
        if (uuidOnWebsite == 0) {
            return addPostArgument(postArguments, "discordId=" + discordMember.getId());
        }
        return postArguments;
    }

    private static String appendProfilePictureURL(String postArguments, JSONObject userInfo, User discordMember) {

        String profilePictureURL = userInfo == null ? "default.png" : userInfo.getString("avatarLink");
        if (profilePictureURL.equals("default.png") || profilePictureURL.equals("dynamic/avatars/default.png")) {
            String addition = "imgurl=" + discordMember.getEffectiveAvatarUrl();

            if (addition.contains("null"))
                return postArguments;

            return addPostArgument(postArguments, addition);
        }
        return postArguments;
    }

    private static String appendRolesL(String postArguments, JSONObject userInfo, Member discordMember) {
        JSONArray roles = userInfo.getJSONArray("roles");

        HashSet<String> rolesUserHasOnWebsite = new HashSet<>();

        //For each role the user has on website
        for (int i = 0; i < roles.length(); i++) {
            JSONObject role = roles.getJSONObject(i);
            rolesUserHasOnWebsite.add(role.getString("name"));
        }


        AtomicBoolean updateRoles = new AtomicBoolean(false);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        //For each role user has in discord
        discordMember.getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.discordRolesToUpdateToWebsite.contains(roleName)) {
                return;
            }

            stringBuilder.append("\"").append(roleName).append("\",");

            //Website currently contains this role already, if it contains all roles already then there is no need to add this to the post
            if (rolesUserHasOnWebsite.contains(roleName)) {
                rolesUserHasOnWebsite.remove(roleName);
                //This return is the syntax for continue in a .forEach loop
                return;
            }

            updateRoles.set(true);
        });
        if (!updateRoles.get() && rolesUserHasOnWebsite.isEmpty())
            return postArguments;

        //Removes the last comma
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");

        return addPostArgument(postArguments, "roles=" + stringBuilder);
    }

    private static String getRolesArguments(Member discordMember) {
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


    private static String addPostArgument(String currentArguments, String addition) {
        if (currentArguments.contains("?")) {
            return currentArguments + "&" + addition;
        } else
            return currentArguments + "?" + addition;
    }

}
