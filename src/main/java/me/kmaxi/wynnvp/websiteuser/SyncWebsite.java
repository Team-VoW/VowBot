package me.kmaxi.wynnvp.websiteuser;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.WynnVPBotMain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static void SyncAllUsers() {

        JSONArray userDataArray = getUsersData("getAllUsers");

        //For each website account
        for (int i = 0; i < 1; i++) {
            JSONObject userInfo = userDataArray.getJSONObject(i);
            SyncUser(userInfo);
        }
    }

    /**
     * Syncs website users if they are in the discord
     * @param userInfo The Json object of the website user
     */
    private static void SyncUser(JSONObject userInfo) {


        String discordUserName = "";

        if (userInfo.get("discord") != null){
            discordUserName = userInfo.getString("discord");
        }

        System.out.println(userInfo.get("discordId"));

        String uuidOnWebsite = "";

        //For some reason a regular null check does not work, so we just check the string value
        if (!userInfo.get("discordId").toString().equals("null")){
            discordUserName = userInfo.getString("discordId");
        }


        Member member = getDiscordMember(uuidOnWebsite, discordUserName);

        if (member == null){
            System.out.println("User " + userInfo.getString("display_name") + " with discord: " + discordUserName + " is not ion the discord");
            return;
        }
        User user = member.getUser();

        String postArguments = "";

        postArguments = appendDiscordUUID(postArguments, uuidOnWebsite, user);

        postArguments = appendProfilePictureURL(postArguments, userInfo, user);

        postArguments = appendRolesL(postArguments, userInfo, discordUserName, member);



        if (postArguments.equals(""))
            return;

        //We always add the discord name and id so that the website can find the correct user.
        if (!postArguments.contains("discord="))
            postArguments = addPostArgument(postArguments, "discord=" + user.getAsTag());
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
     * @param uuid The uuid of the member
     * @param discordUserName The discord name of the member, only used if no person with the UUID was found
     * @return returns the discord member
     */
    private static Member getDiscordMember(String uuid, String discordUserName){
        Guild guild = WynnVPBotMain.guild;

        Member member = null;

        if (!uuid.equals("")){
            member = guild.getMemberById(uuid);
        }


        if (member != null)
            return member;

        if (discordUserName.equals(""))
            return null;


        return guild.getMemberByTag(discordUserName);
    }

    private static String appendDiscordUUID(String postArguments, String uuidOnWebsite, User discordMember){
        if (uuidOnWebsite.equals("") || !discordMember.getId().equals(uuidOnWebsite)) {
            return addPostArgument(postArguments, "discordId=" + discordMember.getId());
        }
        return postArguments;
    }

    private static String appendProfilePictureURL(String postArguments, JSONObject userInfo, User discordMember){
        String profilePictureURL = userInfo.getString("avatarLink");
        if (profilePictureURL.equals("default.png")) {
            return addPostArgument(postArguments, "profilePictureURL=" + discordMember.getAvatarUrl());
        }
        return postArguments;
    }

    private static String appendRolesL(String postArguments, JSONObject userInfo, String discordUserName, Member discordMember){
        JSONArray roles = userInfo.getJSONArray("roles");

        HashSet<String> rolesUserHasOnWebsite = new HashSet<>();
        //For each role the user has on website
        for (int i = 0; i < roles.length(); i++) {
            JSONObject role = roles.getJSONObject(i);
            rolesUserHasOnWebsite.add(role.getString("name"));
        }


        AtomicBoolean updateRoles = new AtomicBoolean(false);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<[");

        //For each role user has in discord
        discordMember.getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.discordRolesToUpdateToWebsite.contains(roleName)){
                return;
            }

            stringBuilder.append("\"").append(roleName).append("\",");

            //Website currently contains this role already, if it contains all roles already then there is no need to add this to the post
            if (rolesUserHasOnWebsite.contains(roleName)) {
                //This return is the syntax for continue in a .forEach loop
                return;
            }

            updateRoles.set(true);
        });
        if (!updateRoles.get())
            return postArguments;

        //Removes the last comma
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]>");

        return addPostArgument(postArguments, stringBuilder.toString());
    }


    private static String addPostArgument(String currentArguments, String addition) {
        if (currentArguments.contains("?")) {
            return currentArguments + "&" + addition;
        } else
            return currentArguments + "?" + addition;
    }

}
