package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Utils {


    public static boolean isStaff(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getIdLong() == Config.roleID
            || role.getIdLong() == Config.writeRoleId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(Member member) {
        return  member.hasPermission(Permission.ADMINISTRATOR);
    }

    public static String convertNumber(int number) {
        switch (number) {
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            case 5:
                return "five";
            case 6:
                return "six";
            case 7:
                return "seven";
            case 8:
                return "eight";
            case 9:
                return "nine";
        }
        return ":x:";
    }

    public static String getUnicode(int number) {
        switch (number) {
            case 1:
                return "\u0031\uFE0F\u20E3";
            case 2:
                return "\u0032\uFE0F\u20E3";
            case 3:
                return "\u0033\uFE0F\u20E3";
            case 4:
                return "\u0034\uFE0F\u20E3";
            case 5:
                return "\u0035\uFE0F\u20E3";
            case 6:
                return "\u0036\uFE0F\u20E3";
            case 7:
                return "\u0037\uFE0F\u20E3";
            case 8:
                return "\u0038\uFE0F\u20E3";
            case 9:
                return "\u0039\uFE0F\u20E3";
            case 0:
                return "\u274C";
        }
        return "\u274C";
    }

    public static int whichNumberWasReacted(String emote) {
        switch (emote) {
            case "\u0031\uFE0F\u20E3":
                return 1;
            case "\u0032\uFE0F\u20E3":
                return 2;
            case "\u0033\uFE0F\u20E3":
                return 3;
            case "\u0034\uFE0F\u20E3":
                return 4;
            case "\u0035\uFE0F\u20E3":
                return 5;
            case "\u0036\uFE0F\u20E3":
                return 6;
            case "\u0037\uFE0F\u20E3":
                return 7;
            case "\u0038\uFE0F\u20E3":
                return 8;
            case "\u0039\uFE0F\u20E3":
                return 9;
        }
        return 0;
    }


    public static void formatName(String name, Guild guild, Consumer<String> callback) {

        if (guild.getCategoryById(Config.spamCategoryID) == null) {
            System.out.println("ERROR! SPAM CATEGORY IS NULL");
            return;
        }

        RemoveMutePerms(guild);


        Objects.requireNonNull(guild.getCategoryById(Config.spamCategoryID))
                .createTextChannel(name)
                .queue(textChannel -> callback.accept(textChannel.getName()));
    }


    public static void sendPrivateMessage(User user, String content) {
        // openPrivateChannel provides a RestAction<PrivateChannel>
        // which means it supplies you with the resulting channel
        user.openPrivateChannel().queue((channel) ->
                channel.sendMessage(content).queue());
    }

    public static void RemoveMutePerms(Guild guild) {
        Objects.requireNonNull(guild.getCategoryById(Config.spamCategoryID)).getRolePermissionOverrides().forEach(permissionOverride -> {
            if (Objects.requireNonNull(permissionOverride.getRole()).getIdLong() == Config.mutedRole) {
                permissionOverride.delete().queue();
            }
        });
    }

    public static boolean hasRole(Member member, long roleToCheck) {
        List<Role> rolesList = member.getRoles();

        for (Role role : rolesList) {
            if (role.getIdLong() == roleToCheck) {
                return true;
            }
        }
        return false;

    }

    public static Collection<Permission> permissions() {
        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.MESSAGE_SEND);
        permissions.add(Permission.MESSAGE_HISTORY);
        permissions.add(Permission.MESSAGE_EMBED_LINKS);
        permissions.add(Permission.MESSAGE_ATTACH_FILES);
        permissions.add(Permission.MESSAGE_ADD_REACTION);
        permissions.add(Permission.MESSAGE_EXT_EMOJI);
        permissions.add(Permission.VIEW_CHANNEL);
        return permissions;
    }

    public static Collection<Permission> traineePerms() {
        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.MESSAGE_HISTORY);
        permissions.add(Permission.MESSAGE_ADD_REACTION);
        return permissions;
    }

    public static Member getFirstMemberWithSpecialPermission(GuildChannel channel) {
        for (PermissionOverride override : channel.getPermissionContainer().getMemberPermissionOverrides()) {
            if (override.isMemberOverride()) {
                Member member = override.getMember();
                if (member != null && !override.getAllowed().isEmpty()) {
                    return member;
                }
            }
        }
        return null;
    }

    public static CompletableFuture<Void> upgradeActorRole(Member member, Guild guild){
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
}
