package me.kmaxi.wynnvp;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Config {

    public static String voteButtonLabel = "Vote";
    public static String removeVoteButtonLabel = "Unvote";

    //MySQL
    public static String host = "localhost";
    public static String port = "3306";
    public static String database = "vow_polls";
    public static String username = "root";

    public static String votesTableName = "votes";

    //Discord server constants /*
    public static long channelName = 820417352617033728l; //TODO what channel's ID?
    public static long roleID = 821772974226407445l; //TODO what role's ID?
    public static long writeRoleId = 819850296926666763l;
    public static long categoryID = 821335417000951858l; //TODO what category's ID?
    public static long closingCategoryID = 867868540320743485l;
    public static long acceptedCategoryID = 821787630726938674l;
    public static long spamCategoryID = 823489138916720670l; //TODO what is "spam" category?
    public static long scriptChannelID = 819850275022962729L;
    public static long traineeRole = 835558288489578536l; //TODO what kind of trainee?
    public static long donatorPlusRole = 824750375764099073l;
    public static long vipRole = 824752537331826748l;
    public static long mutedRole = 897624515985686558l;
    public static long reportedLines = 1135090965938450433l;
    public static long acceptedLines = 955155625296420894l;
    public static long staffBotChat = 956849386489532436l;


    public static String URL_DiscordIntegration = "https://voicesofwynn.com/api/discord-integration";

    //Line reports reaction emojis
    public static String acceptUnicode = "\u2705";
    public static String declineUnicode = "\u274C";
    public static String microphoneUnicode = "\uD83C\uDF99";
    public static String trashUnicode = "\uD83D\uDDD1";

    public static HashSet<String> discordRolesToUpdateToWebsite = new HashSet<>(Arrays.asList(
            "Owner", "Admin", "Developer", "Work Group Manager", "Voice Manager", "Voice Editor",
            "Writer", "Moderator", "Expert Actor", "Skilled Actor", "Top funder",
            "Advanced Actor", "Beginner Actor", "Former Staff"));

    public static ArrayList<String> actorRoleList = new ArrayList<>(Arrays.asList("819550145217298452", "822008829696933909", "821156730079150131", "821157297908744222"));

    private static HashSet<String> vowCloudAccessRoles = new HashSet<>(Arrays.asList(
              "814439316800667651" //Developer
            , "821772974226407445" //Voice Manager
            , "819850296926666763" //Writer
            , "866762348634177616" //Moderator
            , "908327222954328094" //VIP+
            , "824752537331826748" //VIP
            , "824750375764099073" //Donator+
            , "824747069121888266" //Donator
            , "821157297908744222" //Expert Actor
            , "821156730079150131" //Skilled Actor
            , "822008829696933909" //Advanced Actor
            , "819550145217298452" //Beginner Actor
            , "871314322402455582" //Former Staff
            , "868122873003380737" //Official Wynn team
    ));

    public static boolean hasVowCloudAccess(List<Role> roles) {
        for (Role role : roles) {
            if (vowCloudAccessRoles.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }

}
