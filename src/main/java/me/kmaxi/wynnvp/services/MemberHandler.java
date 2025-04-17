package me.kmaxi.wynnvp.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.data.UserService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberHandler {
    private final UserService userService;

    public CompletableFuture<Void> upgradeActorRole(Member member, Guild guild) {
        List<Role> roleList = member.getRoles();
        for (Role role : roleList) {
            if (!Config.ACTOR_ROLE_LIST.contains(role.getId()))
                continue;

            int indexOfRole = Config.ACTOR_ROLE_LIST.indexOf(role.getId());

            //If the user is at the highest role already
            if (indexOfRole == Config.ACTOR_ROLE_LIST.size() - 1) {
                return null;
            }
            guild.removeRoleFromMember(member, role).queue();

            log.info("Upgraded role of {} to actor tier {}", member.getEffectiveName(), indexOfRole + 1);

            return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.ACTOR_ROLE_LIST.get(indexOfRole + 1)))).submit();
        }

        log.info("Added first actor role to {} ", member.getEffectiveName());
        return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.ACTOR_ROLE_LIST.get(0)))).submit();
    }

    public String createAccount(Member member) {
        try {
            return userService.createAccount(member);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
