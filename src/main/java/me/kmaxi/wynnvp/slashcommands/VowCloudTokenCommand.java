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

import static me.kmaxi.wynnvp.Config.hasVowCloudAccess;

public class VowCloudTokenCommand {



    public static void getVowCloudToken(SlashCommandInteractionEvent event) {

        if (!hasVowCloudAccess(event.getMember().getRoles())) {
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



    private static String getReadingUrl(String memberUUID) {
        return "https://voicesofwynn.com/api/premium/load/" + "?discord="
                + memberUUID + "&apiKey=" + APIKeys.vowCloudAPIKey;
    }


}
