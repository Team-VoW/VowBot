package me.kmaxi.wynnvp.services.data;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceTest {

    private static final String API_KEY = "integration-secret";

    private MockRestServiceServer server;
    private UserService userService;

    @BeforeEach
    void setUp() {
        APIKeys apiKeys = new APIKeys();
        apiKeys.discordIntegrationApiKey = API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        userService = new UserService(apiKeys, restTemplate);
    }

    @Test
    void getAllUsersUsesBearerAuthAndMapsResponse() {
        server.expect(once(), requestTo(Config.URL_DISCORD_INTEGRATION + "/users"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andRespond(withSuccess("""
                        [{
                          "userId": 42,
                          "displayName": "WebsiteName",
                          "discordId": "123456789",
                          "discordName": "DiscordName",
                          "avatarUrl": "https://example.com/avatar.png",
                          "pictureType": "Manual",
                          "roleNames": ["Admin", "Writer"]
                        }]
                        """, MediaType.APPLICATION_JSON));

        List<UserDTO> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
        UserDTO user = users.get(0);
        assertThat(user.getUserId()).isEqualTo(42);
        assertThat(user.getDiscordId()).isEqualTo(123456789L);
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(user.getPictureType()).isEqualTo(UserDTO.PictureType.MANUAL);
        assertThat(user.getRoleNames()).containsExactly("Admin", "Writer");
        server.verify();
    }

    @Test
    void setUserIfNeededSendsDiscordAvatarWhenPictureTypeIsDefault() throws Exception {
        UserDTO user = websiteUser(UserDTO.PictureType.DEFAULT, "https://website.example/current.png", List.of());

        server.expect(once(), requestTo(Config.URL_DISCORD_INTEGRATION + "/users/sync"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().json("""
                        {
                          "discordId": "99",
                          "discordName": "DiscordName",
                          "displayName": "WebsiteName",
                          "avatarUrl": "https://cdn.discordapp.com/avatar.png",
                          "roleNames": ["Admin"]
                        }
                        """))
                .andRespond(withSuccess("""
                        {"userId":42,"created":false,"temporaryPassword":null}
                        """, MediaType.APPLICATION_JSON));

        userService.setUserIfNeeded(discordMember(), user);

        server.verify();
    }

    @Test
    void setUserIfNeededKeepsWebsiteAvatarWhenPictureTypeIsManual() throws Exception {
        UserDTO user = websiteUser(UserDTO.PictureType.MANUAL, "https://website.example/manual.png", List.of("Writer"));

        server.expect(once(), requestTo(Config.URL_DISCORD_INTEGRATION + "/users/sync"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "discordId": "99",
                          "discordName": "DiscordName",
                          "displayName": "WebsiteName",
                          "avatarUrl": "https://website.example/manual.png",
                          "roleNames": ["Admin"]
                        }
                        """))
                .andRespond(withSuccess("""
                        {"userId":42,"created":false,"temporaryPassword":null}
                        """, MediaType.APPLICATION_JSON));

        userService.setUserIfNeeded(discordMember(), user);

        server.verify();
    }

    private static UserDTO websiteUser(UserDTO.PictureType pictureType, String avatarUrl, List<String> roles) {
        UserDTO user = new UserDTO();
        user.setDisplayName("WebsiteName");
        user.setDiscordId(99L);
        user.setDiscordName("DiscordName");
        user.setAvatarUrl(avatarUrl);
        user.setPictureType(pictureType);
        user.setRoleNames(roles);
        return user;
    }

    private static Member discordMember() {
        User discordUser = mock(User.class);
        when(discordUser.getName()).thenReturn("DiscordName");
        when(discordUser.getIdLong()).thenReturn(99L);

        net.dv8tion.jda.api.entities.Role adminRole = mock(net.dv8tion.jda.api.entities.Role.class);
        when(adminRole.getName()).thenReturn("Admin");

        Member member = mock(Member.class);
        when(member.getUser()).thenReturn(discordUser);
        when(member.getIdLong()).thenReturn(99L);
        when(member.getEffectiveAvatarUrl()).thenReturn("https://cdn.discordapp.com/avatar.png");
        when(member.getRoles()).thenReturn(List.of(adminRole));
        return member;
    }
}
