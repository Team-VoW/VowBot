
package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import me.kmaxi.wynnvp.utils.MemberUtils;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        UserDTO userDTO = UserDTO.fromMember(member);
        return setUser(userDTO);
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
            return objectMapper.readValue(response, new TypeReference<List<UserDTO>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user data", e);
        }
    }



    public void SetUserIfNeeded(Member discordMember, UserDTO userDTO) {

        if (!shouldUpdate(discordMember, userDTO)) {
            return;
        }

        userDTO.setDiscordName(discordMember.getUser().getName());
        userDTO.setDiscordId(discordMember.getIdLong());
        userDTO.setRoles(MemberUtils.getRoles(discordMember));

        setUser(userDTO);
    }

    private String setUser(UserDTO userDTO) {
        String postArguments = userDTO.getChangingArguments();

        String password = updateUserDataOnWebsite(postArguments);

        // No new account was created.
        if (password.isEmpty()) {
            return "";
        }
        password = extractPassword(password);

        // As this is a new account we send another post request with the profile pic
        postArguments = userDTO.getFullPostArguments();
        updateUserDataOnWebsite(postArguments);

        return password;
    }

    private boolean shouldUpdate(Member discordMember, UserDTO userDTO) {
        if (userDTO.getDiscordId() == 0 || discordMember.getIdLong() != userDTO.getDiscordId()) {
            System.out.println("Discord ID has changed for " + discordMember.getUser().getName() + " was " + userDTO.getDiscordId() + " now " + discordMember.getIdLong());
            return true;
        }
        if (userDTO.getDiscordName() == null || !discordMember.getUser().getName().equals(userDTO.getDiscordName())) {
            System.out.println("Discord name has changed for " + discordMember.getUser().getName() + " was " + userDTO.getDiscordName() + " now " + discordMember.getUser().getName());
            return true;
        }
        // Check if the roles have changed
        List<UserDTO.RoleDTO> memberRoles = MemberUtils.getRoles(discordMember);
        List<UserDTO.RoleDTO> roles = new ArrayList<>(userDTO.getRoles());
        roles.removeAll(memberRoles);
        memberRoles.removeAll(userDTO.getRoles());
        if (roles.isEmpty() && memberRoles.isEmpty()) {
            return false;
        }
        System.out.println("Roles have changed for " + discordMember.getUser().getName());
        //before
        for (UserDTO.RoleDTO role : userDTO.getRoles()) {
            System.out.print(role.getName() + ", ");
        }
        System.out.println();
        //after
        for (UserDTO.RoleDTO role : memberRoles) {
            System.out.print(role.getName() + ", ");
        }
        System.out.println();

        return true;
    }


    /**
     * Can be used both to register a new user based on their Discord account,
     * or to update roles of an existing user with the bot.
     * The system checks whether a user with the specified discordId exists, if not, it checks against discordName.
     * If both queries return nothing, a new user is registered with the provided displayName and discordId.
     *
     * @param urlParameters Post Parameters to use. If an empty string is provided it will do nothing.
     * @return If a new account was created it returns this User's password, if no user was made it returns an empty string.
     */
    private String updateUserDataOnWebsite(String urlParameters) {
        if (urlParameters.isEmpty()) {
            return "";
        }

        urlParameters = addActionAndAPIKey(urlParameters);

        try {

            HttpURLConnection conn = sendPostRequest(Config.URL_DiscordIntegration, urlParameters);
            System.out.println("Response code: " + conn.getResponseCode());
            System.out.println("Response message: " + conn.getResponseMessage());


            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            System.out.println("Result: " + result);
            return result.toString();

            //return response != null ? response : "";
        } catch (Exception e) {
            throw new RuntimeException("Failed to send POST request", e);
        }
    }

    private static String addActionAndAPIKey(String urlParameters) {
        urlParameters += "&action=syncUser";

        // We print before adding the API key to make it easily copyable
        System.out.println("Post parameters: " + urlParameters);

        urlParameters += "&apiKey=" + APIKeys.discordIntegrationAPIKey;
        return urlParameters;
    }

    /**
     * Sends a URL Post request
     *
     * @param requestURL    The url to send the post request to
     * @param urlParameters The parameters to post to the url
     * @return The http connection where you can get response code, response message and other things
     * @throws IOException If an error was encountered
     */
    private static HttpURLConnection sendPostRequest(String requestURL, String urlParameters) throws IOException {
        //Post Request
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }

        return conn;
    }

    private String extractPassword(String input) {
        Pattern pattern = Pattern.compile("\"tempPassword\":\"([a-zA-Z\\d]+)\"");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "ERROR! COULD NOT FIND PASSWORD!";
    }


}