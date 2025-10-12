package me.kmaxi.wynnvp;

import java.util.*;

public class Config {
    // Private constructor to prevent instantiation
    private Config() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String VOTE_BUTTON_LABEL = "Vote";
    public static final String REMOVE_VOTE_BUTTON_LABEL = "Unvote";

    //Wynn Vp Server /*
    public static final long VOICE_APPLY_CHANNEL_ID = 1304017023684050975L;
    public static final long AUDITION_GUIDE_ID = 1426658009974837409L;
    public static final long VOICE_MANGER_ID = 821772974226407445L;
    public static final long WRITE_ROLE_ID = 819850296926666763L;
    public static final long APPLY_CATEGORY_ID = 821335417000951858L;
    public static final long CATEGORY_ID_2 = 1348201751165272074L;
    public static final long CLOSING_CATEGORY_ID = 867868540320743485L;
    public static final long ACCEPTED_CATEGORY_ID = 821787630726938674L;
    public static final long TRAINEE_ROLE = 835558288489578536L;
    public static final long REPORTED_LINES = 1135090965938450433L;
    public static final long STAFF_VOTING_CHANNEL_ID = 846410802777161748L;

    public static final String URL_DISCORD_INTEGRATION = "https://voicesofwynn.com/api/discord-integration";

    public static final String ACCEPT_UNICODE = "✅";
    public static final String DECLINE_UNICODE = "❌";
    public static final String MICROPHONE_UNICODE = "\uD83C\uDF99";
    public static final String TRASH_UNICODE = "\uD83D\uDDD1";

    public static final Set<String> DISCORD_ROLES_TO_UPDATE_TO_WEBSITE = new HashSet<>(Arrays.asList(
            "Owner", "Admin", "Developer", "Cast Manager", "Voice Manager", "Sound Editor",
            "Writer", "Moderator", "Expert Actor", "Skilled Actor", "Top funder",
            "Advanced Actor", "Beginner Actor", "Former Staff"));

    public static final List<String> ACTOR_ROLE_LIST = new ArrayList<>(Arrays.asList("819550145217298452", "822008829696933909", "821156730079150131", "821157297908744222"));
}
