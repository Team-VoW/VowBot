package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.UserDTO;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberUtils {

    public static List<UserDTO.RoleDTO> getRoles(Member discordMember) {
        List<UserDTO.RoleDTO> roles = new ArrayList<>();

        //For each role user has in discord
        discordMember.getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.discordRolesToUpdateToWebsite.contains(roleName)) {
                return;
            }
            roles.add(new UserDTO.RoleDTO(roleName));

        });
        return roles;
    }
}
