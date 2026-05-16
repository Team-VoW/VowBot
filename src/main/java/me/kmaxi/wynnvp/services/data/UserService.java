package me.kmaxi.wynnvp.services.data;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import me.kmaxi.wynnvp.utils.MemberUtils;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class UserService {

    private final APIKeys apiKeys;

    private final RestTemplate restTemplate;

    @Autowired
    public UserService(APIKeys apiKeys) {
        this(apiKeys, new RestTemplate());
    }

    UserService(APIKeys apiKeys, RestTemplate restTemplate) {
        this.apiKeys = apiKeys;
        this.restTemplate = restTemplate;
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
        return setUser(userDTO);
    }

    /**
     * Fetches all users and returns them as a list of UserDTO objects.
     *
     * @return List of UserDTO objects representing all users.
     */
    public List<UserDTO> getAllUsers() {
        ResponseEntity<List<UserDTO>> responseEntity = restTemplate.exchange(
                Config.URL_DISCORD_INTEGRATION + "/users",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                new ParameterizedTypeReference<>() {
                }
        );

        List<UserDTO> users = responseEntity.getBody();
        return users != null ? users : List.of();
    }


    public void setUserIfNeeded(Member discordMember, UserDTO userDTO) throws IOException {

        if (!shouldUpdate(discordMember, userDTO)) {
            return;
        }

        userDTO.setDiscordName(discordMember.getUser().getName());
        userDTO.setDiscordId(discordMember.getIdLong());
        userDTO.setRoleNames(MemberUtils.getRoleNames(discordMember));
        if (shouldSyncDiscordPicture(userDTO)) {
            userDTO.setAvatarUrl(discordMember.getEffectiveAvatarUrl());
        }

        setUser(userDTO);
    }

    private String setUser(UserDTO userDTO) {
        SyncDiscordUserResponse response = updateUserDataOnWebsite(userDTO);
        return response.temporaryPassword() != null ? response.temporaryPassword() : "";
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
        if (shouldSyncDiscordPicture(userDTO)) {
            log.info("Avatar should sync from Discord for {}", discordMember.getUser().getName());
            return true;
        }

        // Check if the roles have changed
        List<String> memberRoles = MemberUtils.getRoleNames(discordMember);
        List<String> websiteRoles = userDTO.getRoleNames() != null ? userDTO.getRoleNames() : List.of();
        List<String> roles = new ArrayList<>(websiteRoles);
        roles.removeAll(memberRoles);
        memberRoles.removeAll(websiteRoles);
        if (roles.isEmpty() && memberRoles.isEmpty()) {
            return false;
        }
        log.info("Roles have changed for {} was {} now {}", discordMember.getUser().getName(), userDTO.getRoleNames(), memberRoles);

        return true;
    }


    /**
     * Can be used both to register a new user based on their Discord account,
     * or to update roles of an existing user with the bot.
     * The system checks whether a user with the specified discordId exists, if not, it checks against discordName.
     * If both queries return nothing, a new user is registered with the provided displayName and discordId.
     *
     * @return If a new account was created it returns this User's password, if no user was made it returns an empty string.
     */
    private SyncDiscordUserResponse updateUserDataOnWebsite(UserDTO userDTO) {
        HttpEntity<SyncDiscordUserRequest> requestEntity = new HttpEntity<>(SyncDiscordUserRequest.from(userDTO), createHeaders());

        ResponseEntity<SyncDiscordUserResponse> responseEntity = restTemplate.postForEntity(
                Config.URL_DISCORD_INTEGRATION + "/users/sync",
                requestEntity,
                SyncDiscordUserResponse.class
        );
        log.debug("Response code: {}", responseEntity.getStatusCode().value());

        SyncDiscordUserResponse response = responseEntity.getBody();
        log.debug("Result: {}", response);
        return response != null ? response : new SyncDiscordUserResponse(0, false, null);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKeys.discordIntegrationApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private boolean shouldSyncDiscordPicture(UserDTO userDTO) {
        return userDTO.getPictureType() == null || userDTO.getPictureType() == UserDTO.PictureType.DEFAULT;
    }

    public UserDTO fromMember(Member member) {
        UserDTO userDTO = new UserDTO();
        userDTO.setDiscordName(member.getUser().getName());
        userDTO.setDiscordId(member.getUser().getIdLong());
        userDTO.setAvatarUrl(member.getEffectiveAvatarUrl());
        userDTO.setPictureType(UserDTO.PictureType.DEFAULT);
        userDTO.setRoleNames(MemberUtils.getRoleNames(member));

        return userDTO;
    }

    private record SyncDiscordUserRequest(
            String discordId,
            String discordName,
            String displayName,
            String avatarUrl,
            List<String> roleNames
    ) {
        private static SyncDiscordUserRequest from(UserDTO userDTO) {
            List<String> roleNames = userDTO.getRoleNames() != null ? userDTO.getRoleNames() : List.of();

            String displayName = userDTO.getDisplayName() != null ? userDTO.getDisplayName() : userDTO.getDiscordName();

            return new SyncDiscordUserRequest(
                    Long.toString(userDTO.getDiscordId()),
                    userDTO.getDiscordName(),
                    displayName,
                    userDTO.getAvatarUrl(),
                    roleNames
            );
        }
    }

    private record SyncDiscordUserResponse(int userId, boolean created, String temporaryPassword) {
    }
}
