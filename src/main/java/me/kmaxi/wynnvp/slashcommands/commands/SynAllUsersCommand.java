package me.kmaxi.wynnvp.slashcommands.commands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.BotRegister;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.kmaxi.wynnvp.utils.APIUtils.getJsonData;
import static me.kmaxi.wynnvp.utils.APIUtils.updateUserDataOnWebsite;
@Component
public class SynAllUsersCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("syncallusers", "Syncs all users data to the website. Warning, this is a heavy command!");
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        JSONArray userDataArray = getUsersData("getAllUsers");


        event.deferReply().queue();

        //For each website account
        for (int i = 0; i < userDataArray.length(); i++) {
            JSONObject userInfo = userDataArray.getJSONObject(i);

            SyncUser(userInfo);
        }

        event.getHook().setEphemeral(true).editOriginal("Synced all users").queue();
    }

    /**
     * @param action "getAllUsers" returns all
     * @return Returns the users' data from the website
     */
    private JSONArray getUsersData(String action) {

        try {
            return getJsonData(Config.URL_DiscordIntegration + "?action=" + action + "&apiKey=" + APIKeys.discordIntegrationAPIKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Syncs website users if they are in the discord
     *
     * @param userInfo The Json object of the website user
     */
    private void SyncUser(JSONObject userInfo) {
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
    private Member getDiscordMember(long uuid, String discordUserName) {
        Guild guild = BotRegister.guild;

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

    private String appendRolesL(String postArguments, JSONObject userInfo, Member discordMember) {
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

    private String appendDiscordUUID(String postArguments, long uuidOnWebsite, User discordMember) {
        if (uuidOnWebsite == 0) {
            return addPostArgument(postArguments, "discordId=" + discordMember.getId());
        }
        return postArguments;
    }

    private String addPostArgument(String currentArguments, String addition) {
        if (currentArguments.contains("?")) {
            return currentArguments + "&" + addition;
        } else
            return currentArguments + "?" + addition;
    }

    private  String appendProfilePictureURL(String postArguments, JSONObject userInfo, User discordMember) {

        String profilePictureURL = userInfo == null ? "default.png" : userInfo.getString("avatarLink");
        if (profilePictureURL.equals("default.png") || profilePictureURL.equals("dynamic/avatars/default.png")) {
            String addition = "imgurl=" + discordMember.getEffectiveAvatarUrl();

            if (addition.contains("null"))
                return postArguments;

            return addPostArgument(postArguments, addition);
        }
        return postArguments;
    }
}
