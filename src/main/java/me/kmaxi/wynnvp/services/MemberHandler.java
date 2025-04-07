package me.kmaxi.wynnvp.services;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.data.UserService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class MemberHandler {
    @Autowired
    private UserService userService;
    public CompletableFuture<Void> upgradeActorRole(Member member, Guild guild) {
        List<Role> roleList = member.getRoles();
        for (Role role : roleList) {
            if (!Config.actorRoleList.contains(role.getId()))
                continue;

            int indexOfRole = Config.actorRoleList.indexOf(role.getId());

            //If the user is at the highest role already
            if (indexOfRole == Config.actorRoleList.size() - 1) {
                return null;
            }
            guild.removeRoleFromMember(member, role).queue();

            System.out.println("Upgraded role of " + member.getEffectiveName() + " to actor tier " + indexOfRole + 1);

            return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.actorRoleList.get(indexOfRole + 1)))).submit();
        }

        System.out.println("Added first actor role to " + member.getEffectiveName());
        return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.actorRoleList.get(0)))).submit();
    }

    public String createAccount(Member member){
        try {
            return userService.createAccount(member);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
