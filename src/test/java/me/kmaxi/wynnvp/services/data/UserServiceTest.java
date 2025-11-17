package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private APIKeys apiKeys;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserService userService;

    @Mock
    private Member member;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        // Inject the mocked dependencies using reflection
        ReflectionTestUtils.setField(userService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(userService, "objectMapper", objectMapper);

        apiKeys.discordIntegrationApiKey = "test-api-key";

        // Setup common Member mock behavior
        when(member.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("TestUser");
        when(user.getIdLong()).thenReturn(123456789L);
        when(member.getIdLong()).thenReturn(123456789L);
        when(member.getEffectiveAvatarUrl()).thenReturn("https://cdn.discordapp.com/avatars/123/abc.png");
        when(member.getRoles()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Should create account successfully and return password")
    void createAccount_Success() throws IOException {
        // Given
        String expectedPassword = "TempPass123";
        String apiResponse = "{\"tempPassword\":\"" + expectedPassword + "\"}";

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK))
          .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        // When
        String actualPassword = userService.createAccount(member);

        // Then
        assertThat(actualPassword).isEqualTo(expectedPassword);
        verify(restTemplate, times(2)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should return empty string when account already exists")
    void createAccount_AccountExists() throws IOException {
        // Given
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        // When
        String password = userService.createAccount(member);

        // Then
        assertThat(password).isEmpty();
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should fetch all users successfully")
    void getAllUsers_Success() {
        // Given
        String jsonResponse = "[{\"discordName\":\"User1\",\"discordId\":111},{\"discordName\":\"User2\",\"discordId\":222}]";
        String expectedUrl = Config.URL_DISCORD_INTEGRATION + "?action=getAllUsers&apiKey=test-api-key";

        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(jsonResponse);

        // When
        List<UserDTO> users = userService.getAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getDiscordName()).isEqualTo("User1");
        assertThat(users.get(1).getDiscordName()).isEqualTo("User2");
        verify(restTemplate).getForObject(expectedUrl, String.class);
    }

    @Test
    @DisplayName("Should throw RuntimeException when JSON parsing fails")
    void getAllUsers_ParseError() {
        // Given
        String invalidJson = "invalid json";
        String expectedUrl = Config.URL_DISCORD_INTEGRATION + "?action=getAllUsers&apiKey=test-api-key";

        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(invalidJson);

        // When/Then
        assertThatThrownBy(() -> userService.getAllUsers())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse user data");
    }

    @Test
    @DisplayName("Should extract password correctly from JSON response")
    void extractPassword_Success() throws Exception {
        // Given
        String jsonResponse = "{\"status\":\"success\",\"tempPassword\":\"SecurePass456\",\"userId\":123}";

        // Use reflection to access private method
        java.lang.reflect.Method method = UserService.class.getDeclaredMethod("extractPassword", String.class);
        method.setAccessible(true);

        // When
        String password = (String) method.invoke(userService, jsonResponse);

        // Then
        assertThat(password).isEqualTo("SecurePass456");
    }

    @Test
    @DisplayName("Should return error message when password not found in response")
    void extractPassword_NotFound() throws Exception {
        // Given
        String jsonResponse = "{\"status\":\"error\"}";

        // Use reflection to access private method
        java.lang.reflect.Method method = UserService.class.getDeclaredMethod("extractPassword", String.class);
        method.setAccessible(true);

        // When
        String password = (String) method.invoke(userService, jsonResponse);

        // Then
        assertThat(password).isEqualTo("ERROR! COULD NOT FIND PASSWORD!");
    }

    @Test
    @DisplayName("Should convert Member to UserDTO correctly")
    void fromMember_Success() {
        // When
        UserDTO userDTO = userService.fromMember(member);

        // Then
        assertThat(userDTO.getDiscordName()).isEqualTo("TestUser");
        assertThat(userDTO.getDiscordId()).isEqualTo(123456789L);
        assertThat(userDTO.getAvatarLink()).isEqualTo("https://cdn.discordapp.com/avatars/123/abc.png");
        assertThat(userDTO.getRoles()).isNotNull();
    }

    @Test
    @DisplayName("Should update user when Discord ID has changed")
    void setUserIfNeeded_DiscordIdChanged() throws IOException {
        // Given
        UserDTO existingUser = new UserDTO();
        existingUser.setDiscordId(999999L); // Different ID
        existingUser.setDiscordName("OldName");
        existingUser.setAvatarLink("https://old-avatar.png");
        existingUser.setRoles(Collections.emptyList());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        // When
        userService.setUserIfNeeded(member, existingUser);

        // Then
        assertThat(existingUser.getDiscordId()).isEqualTo(123456789L);
        assertThat(existingUser.getDiscordName()).isEqualTo("TestUser");
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should update user when Discord name has changed")
    void setUserIfNeeded_NameChanged() throws IOException {
        // Given
        UserDTO existingUser = new UserDTO();
        existingUser.setDiscordId(123456789L);
        existingUser.setDiscordName("OldName");
        existingUser.setAvatarLink("https://old-avatar.png");
        existingUser.setRoles(Collections.emptyList());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        // When
        userService.setUserIfNeeded(member, existingUser);

        // Then
        assertThat(existingUser.getDiscordName()).isEqualTo("TestUser");
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should not update when user data is already current")
    void setUserIfNeeded_NoUpdateNeeded() throws IOException {
        // Given
        UserDTO currentUser = new UserDTO();
        currentUser.setDiscordId(123456789L);
        currentUser.setDiscordName("TestUser");
        currentUser.setAvatarLink("https://cdn.discordapp.com/avatars/123/abc.png");
        currentUser.setRoles(Collections.emptyList());

        // When
        userService.setUserIfNeeded(member, currentUser);

        // Then
        verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Should update user when avatar is default")
    void setUserIfNeeded_DefaultAvatar() throws IOException {
        // Given
        UserDTO existingUser = new UserDTO();
        existingUser.setDiscordId(123456789L);
        existingUser.setDiscordName("TestUser");
        existingUser.setAvatarLink("dynamic/avatars/default.png");
        existingUser.setRoles(Collections.emptyList());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        // When
        userService.setUserIfNeeded(member, existingUser);

        // Then
        assertThat(existingUser.getAvatarLink()).isEqualTo("https://cdn.discordapp.com/avatars/123/abc.png");
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }
}
