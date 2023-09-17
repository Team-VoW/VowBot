package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.utils.APIUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;

public class VowCloudTokenCommand {

    private static HashSet<String> vowCloudTokens = new HashSet<>(Arrays.asList(
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

    public static void getVowCloudToken(SlashCommandInteractionEvent event) {

        if (!hasVowCloudAccess(event.getMember())) {
            event.reply(
                            "You do not have access to VowCloud. Get access by becoming a voice actor or by supporting us on Patreon: https://www.patreon.com/Voices_Of_Wynn")
                    .setEphemeral(true).queue();
            return;
        }

        try {
            JSONObject data = APIUtils.getJsonObject(getReadingUrl(event.getUser().getId()));
            String token = data.getString("code");

            if (token == null) {
                event.reply(
                                "Token was null. JsonArray: " + data)
                        .setEphemeral(true).queue();
                return;
            }

            event.reply("Your token is: " + token).setEphemeral(true).queue();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasVowCloudAccess(Member member) {
        for (Role role : member.getRoles()) {
            if (vowCloudTokens.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }


    private static String getReadingUrl(String memberUUID) {
        return "https://voicesofwynn.com/api/premium/load/" + "?discord="
                + memberUUID + "&apiKey=" + APIKeys.vowCloudAPIKey;
    }


}
