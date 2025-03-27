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

    //My sql
    public static String host = "66.11.118.47";
    public static String port = "3306";
    public static String database = "s10530_polls";
    public static String username = "u10530_NJoqWZbXbX";

    public static String votesTableName = "votes";

    //Wynn Vp Server /*
    public static long channelName = 1304017023684050975l;
    public static long roleID = 821772974226407445l;
    public static long writeRoleId = 819850296926666763l;
    public static long categoryID = 821335417000951858l;
    public static long categoryID2 = 1348201751165272074l;
    public static long closingCategoryID = 867868540320743485l;
    public static long acceptedCategoryID = 821787630726938674l;
    public static long spamCategoryID = 823489138916720670l;
    public static long scriptChannelID = 819850275022962729L;
    public static long traineeRole = 835558288489578536l;
    public static long donatorPlusRole = 824750375764099073l;
    public static long vipRole = 824752537331826748l;
    public static long mutedRole = 897624515985686558l;
    public static long reportedLines = 1135090965938450433l;
    public static long acceptedLines = 955155625296420894l;
    public static long staffBotChat = 956849386489532436l;


    public static String URL_DiscordIntegration = "https://voicesofwynn.com/api/discord-integration";

    public static String acceptUnicode = "\u2705";
    public static String declineUnicode = "\u274C";
    public static String microphoneUnicode = "\uD83C\uDF99";
    public static String trashUnicode = "\uD83D\uDDD1";

    public static HashSet<String> discordRolesToUpdateToWebsite = new HashSet<>(Arrays.asList(
            "Owner", "Admin", "Developer", "Cast Manager", "Voice Manager", "Sound Editor",
            "Writer", "Moderator", "Expert Actor", "Skilled Actor", "Top funder",
            "Advanced Actor", "Beginner Actor", "Former Staff"));

    public static ArrayList<String> actorRoleList = new ArrayList<>(Arrays.asList("819550145217298452", "822008829696933909", "821156730079150131", "821157297908744222"));



}
