
package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.kmaxi.wynnvp.utils.APIUtils.updateUserDataOnWebsite;

@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UserService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    /**
     * Creates an account for the given member and returns the password.
     * The password is empty if the account already exists.
     *
     * @param member the Discord member for whom the account is being created
     * @return the password for the new account, or an empty string if the account already exists
     * @throws IOException if an I/O error occurs
     */
    public String createAccount(Member member) throws IOException {
        String postArguments = "";

        postArguments = addPostArgument(postArguments, "discordName=" + member.getUser().getAsTag());
        postArguments = addPostArgument(postArguments, "discordId=" + member.getUser().getId());
        postArguments = addPostArgument(postArguments, "roles=" + getRolesArguments(member));

        String password = updateUserDataOnWebsite(postArguments);

        // No new account was created.
        if (password.isEmpty()) {
            return "";
        }
        password = extractPassword(password);

        // As this is a new account we send another post request with the profile pic
        postArguments = appendProfilePictureURL(postArguments, null, member.getUser());
        updateUserDataOnWebsite(postArguments);

        return password;
    }

    /**
     * Fetches all users and returns them as a list of UserDTO objects.
     *
     * @return List of UserDTO objects representing all users.
     */
    public List<UserDTO> getAllUsers() {
        String url = Config.URL_DiscordIntegration + "?action=getAllUsers&apiKey=" + APIKeys.discordIntegrationAPIKey;
        String response = restTemplate.getForObject(url, String.class);
        try {
            return objectMapper.readValue(response, new TypeReference<List<UserDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user data", e);
        }
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