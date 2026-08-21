package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberUtilsTest {

    @Test
    void mapsCastManagerByDiscordRoleId() {
        Role castManagerRole = mock(Role.class);
        when(castManagerRole.getIdLong()).thenReturn(Config.CAST_MANAGER_ROLE_ID);
        when(castManagerRole.getName()).thenReturn("Renamed role");

        Member member = mock(Member.class);
        when(member.getRoles()).thenReturn(List.of(castManagerRole));

        assertThat(MemberUtils.getRoleNames(member)).containsExactly(Config.CAST_MANAGER_ROLE_NAME);
    }
}
