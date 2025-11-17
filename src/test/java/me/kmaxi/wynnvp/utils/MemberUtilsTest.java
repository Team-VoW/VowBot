package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberUtils Tests")
class MemberUtilsTest {

    @Mock
    private Member member;

    @Mock
    private Role allowedRole1;

    @Mock
    private Role allowedRole2;

    @Mock
    private Role disallowedRole;

    @BeforeEach
    void setUp() {
        // Setup allowed roles
        when(allowedRole1.getName()).thenReturn("Voice Actor");
        when(allowedRole2.getName()).thenReturn("Trainee Actor");

        // Setup disallowed role
        when(disallowedRole.getName()).thenReturn("Some Random Role");
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when attempting to instantiate")
    void constructor_ThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = MemberUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("This is a utility class and cannot be instantiated");
    }

    @Test
    @DisplayName("Should return empty list when member has no roles")
    void getRoles_NoRoles_ReturnsEmptyList() {
        // Given
        when(member.getRoles()).thenReturn(Collections.emptyList());

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should filter and return only allowed roles")
    void getRoles_MixedRoles_ReturnsOnlyAllowed() {
        // Given
        when(member.getRoles()).thenReturn(Arrays.asList(allowedRole1, disallowedRole, allowedRole2));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDTO.RoleDTO::getName)
                    .containsExactlyInAnyOrder("Voice Actor", "Trainee Actor");
        }
    }

    @Test
    @DisplayName("Should return all roles when all are allowed")
    void getRoles_AllAllowed_ReturnsAll() {
        // Given
        when(member.getRoles()).thenReturn(Arrays.asList(allowedRole1, allowedRole2));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Test
    @DisplayName("Should return empty list when no roles are allowed")
    void getRoles_NoneAllowed_ReturnsEmpty() {
        // Given
        when(member.getRoles()).thenReturn(Arrays.asList(disallowedRole));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should handle member with single allowed role")
    void getRoles_SingleAllowedRole_ReturnsSingle() {
        // Given
        when(member.getRoles()).thenReturn(Collections.singletonList(allowedRole1));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Voice Actor");
        }
    }

    @Test
    @DisplayName("Should handle case-sensitive role name matching")
    void getRoles_CaseSensitive() {
        // Given
        Role roleWithDifferentCase = mock(Role.class);
        when(roleWithDifferentCase.getName()).thenReturn("voice actor"); // lowercase

        when(member.getRoles()).thenReturn(Collections.singletonList(roleWithDifferentCase));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor")); // uppercase V and A

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then - Should not match due to case sensitivity
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Should preserve role order from member's role list")
    void getRoles_PreservesOrder() {
        // Given
        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);
        Role role3 = mock(Role.class);

        when(role1.getName()).thenReturn("Expert Actor");
        when(role2.getName()).thenReturn("Voice Actor");
        when(role3.getName()).thenReturn("Trainee Actor");

        when(member.getRoles()).thenReturn(Arrays.asList(role1, role2, role3));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Expert Actor", "Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserDTO.RoleDTO::getName)
                    .containsExactly("Expert Actor", "Voice Actor", "Trainee Actor");
        }
    }

    @Test
    @DisplayName("Should create RoleDTO objects with correct role names")
    void getRoles_CreatesCorrectRoleDTOs() {
        // Given
        when(member.getRoles()).thenReturn(Collections.singletonList(allowedRole1));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Collections.singletonList("Voice Actor"));

            // When
            List<UserDTO.RoleDTO> result = MemberUtils.getRoles(member);

            // Then
            assertThat(result).hasSize(1);
            UserDTO.RoleDTO roleDTO = result.get(0);
            assertThat(roleDTO).isNotNull();
            assertThat(roleDTO.getName()).isEqualTo("Voice Actor");
        }
    }

    @Test
    @DisplayName("Should handle multiple members independently")
    void getRoles_MultipleMembersIndependent() {
        // Given
        Member member2 = mock(Member.class);
        when(member.getRoles()).thenReturn(Collections.singletonList(allowedRole1));
        when(member2.getRoles()).thenReturn(Collections.singletonList(allowedRole2));

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE)
                    .thenReturn(Arrays.asList("Voice Actor", "Trainee Actor"));

            // When
            List<UserDTO.RoleDTO> result1 = MemberUtils.getRoles(member);
            List<UserDTO.RoleDTO> result2 = MemberUtils.getRoles(member2);

            // Then
            assertThat(result1).hasSize(1);
            assertThat(result1.get(0).getName()).isEqualTo("Voice Actor");

            assertThat(result2).hasSize(1);
            assertThat(result2.get(0).getName()).isEqualTo("Trainee Actor");
        }
    }
}
