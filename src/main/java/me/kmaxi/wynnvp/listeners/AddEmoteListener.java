package me.kmaxi.wynnvp.listeners;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.audition.AuditionsChannelHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller @RequiredArgsConstructor
public class AddEmoteListener extends ListenerAdapter {

    private final AuditionsChannelHandler auditionsChannelHandler;

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() == Config.VOICE_APPLY_CHANNEL_ID) {
            voiceApplyReact(event);
        }
    }

    private void voiceApplyReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        char letterReacted = Utils.whichLetterWasReacted(event.getEmoji().getName());

        String[] messageArray = message.split("\n");

        String line = messageArray[(2 * numberReacted) - 1];
        String[] splitLine = line.split("=");

        String npcName = splitLine[1].trim()

        String questName = messageArray[0].replace("React to apply for a role in", "").replace(">>>", "").replace("**", "").trim();
        
        auditionsChannelHandler.openAudition(questName, npcName, Objects.requireNonNull(event.getMember()));
    }
}


