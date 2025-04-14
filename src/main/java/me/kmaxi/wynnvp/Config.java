package me.kmaxi.wynnvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Config {
    public static String voteButtonLabel = "Vote";
    public static String removeVoteButtonLabel = "Unvote";

    //Wynn Vp Server /*
    public static long voiceApplyChannelId = 1304017023684050975L;
    public static long voiceMangerId = 821772974226407445L;
    public static long writeRoleId = 819850296926666763L;
    public static long applyCategoryId = 821335417000951858L;
    public static long categoryID2 = 1348201751165272074L;
    public static long closingCategoryID = 867868540320743485L;
    public static long acceptedCategoryID = 821787630726938674L;
    public static long traineeRole = 835558288489578536L;
    public static long reportedLines = 1135090965938450433L;

    public static String URL_DiscordIntegration = "https://voicesofwynn.com/api/discord-integration";

    public static String acceptUnicode = "✅";
    public static String declineUnicode = "❌";
    public static String microphoneUnicode = "\uD83C\uDF99";
    public static String trashUnicode = "\uD83D\uDDD1";

    public static HashSet<String> discordRolesToUpdateToWebsite = new HashSet<>(Arrays.asList(
            "Owner", "Admin", "Developer", "Cast Manager", "Voice Manager", "Sound Editor",
            "Writer", "Moderator", "Expert Actor", "Skilled Actor", "Top funder",
            "Advanced Actor", "Beginner Actor", "Former Staff"));

    public static ArrayList<String> actorRoleList = new ArrayList<>(Arrays.asList("819550145217298452", "822008829696933909", "821156730079150131", "821157297908744222"));


}
