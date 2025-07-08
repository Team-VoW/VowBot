package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import me.kmaxi.wynnvp.utils.MemberUtils;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class UserService {

    private final APIKeys apiKeys;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String defaultDiscordAvatar = "dynamic/avatars/default.png";

    public UserService(APIKeys apiKeys) {
        this.apiKeys = apiKeys;
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
        UserDTO userDTO = fromMember(member);
        return setUser(userDTO, true);
    }

    /**
     * Fetches all users and returns them as a list of UserDTO objects.
     *
     * @return List of UserDTO objects representing all users.
     */
    public List<UserDTO> getAllUsers() {
        String url = Config.URL_DISCORD_INTEGRATION + "?action=getAllUsers&apiKey=" + apiKeys.discordIntegrationApiKey;
        String response = restTemplate.getForObject(url, String.class);
        try {
            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user data", e);
        }
    }


    public void setUserIfNeeded(Member discordMember, UserDTO userDTO) throws IOException {

        if (!shouldUpdate(discordMember, userDTO)) {
            return;
        }

        userDTO.setDiscordName(discordMember.getUser().getName());
        userDTO.setDiscordId(discordMember.getIdLong());
        userDTO.setRoles(MemberUtils.getRoles(discordMember));
        boolean syncProfilePic = false;
        if (userDTO.getAvatarLink() == null || userDTO.getAvatarLink().equals(defaultDiscordAvatar)) {
            userDTO.setAvatarLink(discordMember.getEffectiveAvatarUrl());
            syncProfilePic = true;
        }

        setUser(userDTO, syncProfilePic);
    }

    private String setUser(UserDTO userDTO, boolean syncProfilePic) {
        String postArguments = syncProfilePic ? userDTO.getFullPostArguments() : userDTO.getChangingArguments();

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
            log.info("Discord ID has changed for {} was {} now {}", discordMember.getUser().getName(), userDTO.getDiscordId(), discordMember.getIdLong());
            return true;
        }
        if (userDTO.getDiscordName() == null || !discordMember.getUser().getName().equals(userDTO.getDiscordName())) {
            log.info("Discord name has changed for {} was {} now {}", discordMember.getUser().getName(), userDTO.getDiscordName(), discordMember.getUser().getName());
            return true;
        }
        if (userDTO.getAvatarLink() == null || userDTO.getAvatarLink().equals(defaultDiscordAvatar)) {
            log.info("Avatar is unset for {} was {}", discordMember.getUser().getName(), userDTO.getAvatarLink());
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
        log.info("Roles have changed for {} was {} now {}", discordMember.getUser().getName(), userDTO.getRoles(), memberRoles);

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("charset", "utf-8");

        HttpEntity<String> requestEntity = new HttpEntity<>(urlParameters, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(Config.URL_DISCORD_INTEGRATION, requestEntity, String.class);

        log.debug("Response code: {}", responseEntity.getStatusCode().value());

        String result = responseEntity.getBody();
        log.debug("Result: {}", result);
        return result != null ? result : "";
    }

    private String addActionAndAPIKey(String urlParameters) {
        urlParameters += "&action=syncUser";

        log.debug("Post parameters: {}", urlParameters);

        urlParameters += "&apiKey=" + apiKeys.discordIntegrationApiKey;
        return urlParameters;
    }


    private String extractPassword(String input) {
        Pattern pattern = Pattern.compile("\"tempPassword\":\"([a-zA-Z\\d]+)\"");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "ERROR! COULD NOT FIND PASSWORD!";
    }

    public UserDTO fromMember(Member member) {
        UserDTO userDTO = new UserDTO();
        userDTO.setDiscordName(member.getUser().getName());
        userDTO.setDiscordId(member.getUser().getIdLong());
        userDTO.setAvatarLink(member.getEffectiveAvatarUrl());
        userDTO.setRoles(MemberUtils.getRoles(member));

        return userDTO;
    }


}