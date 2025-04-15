package me.kmaxi.wynnvp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Config {
    public static final String voteButtonLabel = "Vote";
    public static final String removeVoteButtonLabel = "Unvote";

    //Wynn Vp Server /*
    public static final long voiceApplyChannelId = 1304017023684050975L;
    public static final long voiceMangerId = 821772974226407445L;
    public static final long writeRoleId = 819850296926666763L;
    public static final long applyCategoryId = 821335417000951858L;
    public static final long categoryID2 = 1348201751165272074L;
    public static final long closingCategoryID = 867868540320743485L;
    public static final long acceptedCategoryID = 821787630726938674L;
    public static final long traineeRole = 835558288489578536L;
    public static final long reportedLines = 1135090965938450433L;

    public static final String URL_DiscordIntegration = "https://voicesofwynn.com/api/discord-integration";

    public static final String acceptUnicode = "✅";
    public static final String declineUnicode = "❌";
    public static final String microphoneUnicode = "\uD83C\uDF99";
    public static final String trashUnicode = "\uD83D\uDDD1";

    public static final HashSet<String> discordRolesToUpdateToWebsite = new HashSet<>(Arrays.asList(
            "Owner", "Admin", "Developer", "Cast Manager", "Voice Manager", "Sound Editor",
            "Writer", "Moderator", "Expert Actor", "Skilled Actor", "Top funder",
            "Advanced Actor", "Beginner Actor", "Former Staff"));

    public static final ArrayList<String> actorRoleList = new ArrayList<>(Arrays.asList("819550145217298452", "822008829696933909", "821156730079150131", "821157297908744222"));
}