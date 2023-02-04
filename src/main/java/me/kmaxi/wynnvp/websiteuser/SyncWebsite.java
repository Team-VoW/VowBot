package me.kmaxi.wynnvp.websiteuser;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.WynnVPBotMain;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static me.kmaxi.wynnvp.utils.APIUtils.getJsonData;

public class SyncWebsite {

    private static HashMap<String, Member> usernameMember;

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

    public static void fillCachedUsernameMembers() {
        usernameMember = new HashMap<>();

        AtomicReference<ArrayList<Member>> userList = new AtomicReference<>(new ArrayList<>());


        WynnVPBotMain.guild.loadMembers().onSuccess(members -> {
            userList.set((ArrayList<Member>) members);
        });

        System.out.println("Retrieved " + userList.get().size() + " members.");

        for (Member member : userList.get()) {
            usernameMember.put(member.getUser().getAsTag(), member);
        }
    }

    public static void SyncAllUsers() {

        fillCachedUsernameMembers();
        JSONArray userDataArray = getUsersData("getAllUsers");


        //For each website account
        for (int i = 0; i < 1; i++) {
            JSONObject userInfo = userDataArray.getJSONObject(i);
            SyncUser(userInfo);


        }
    }


    private static void SyncUser(JSONObject userInfo) {
        String discordUserName = userInfo.getString("discord");

        if (!usernameMember.containsKey(discordUserName)) {
            System.out.println(discordUserName + " is not in the discord");
            return;
        }

        User discordMember = usernameMember.get(discordUserName).getUser();

        String postArguments = "";

        postArguments = appendDiscordUUID(postArguments, userInfo, discordMember);

        postArguments = appendProfilePictureURL(postArguments, userInfo, discordMember);

        postArguments = appendRolesL(postArguments, userInfo, discordUserName);

        if (postArguments.equals(""))
            return;

        postArguments = addPostArgument(postArguments, "discordName=" + discordUserName);


        System.out.println("Post arguments would be: " + postArguments);

    }

    private static String appendDiscordUUID(String postArguments, JSONObject userInfo, User discordMember){
        String discordUUID = userInfo.getString("discordId");
        if (discordUUID.equals("")) {
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

    private static String appendRolesL(String postArguments, JSONObject userInfo, String discordUserName){
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
        usernameMember.get(discordUserName).getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.discordRolesToUpdateToWebsite.contains(roleName)){
                return;
            }

            stringBuilder.append("\"").append(roleName).append("\",");

            //Website currently contains this role already, if it contains all roles already then there is no need to add this to the post
            if (rolesUserHasOnWebsite.contains(roleName)) {
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
