package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberUtils {
    private MemberUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static List<String> getRoleNames(Member discordMember) {
        List<String> roleNames = new ArrayList<>();

        //For each role user has in discord
        discordMember.getRoles().forEach(role -> {
            String roleName = role.getName();

            //Is a weird role that should not be published to the website
            if (!Config.DISCORD_ROLES_TO_UPDATE_TO_WEBSITE.contains(roleName)) {
                return;
            }
            roleNames.add(roleName);

        });
        return roleNames;
    }
}
