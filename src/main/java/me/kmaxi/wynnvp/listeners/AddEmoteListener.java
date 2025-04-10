package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.services.AuditionsChannelHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.Objects;

@Controller
public class AddEmoteListener extends ListenerAdapter {

    @Autowired
    private AuditionsChannelHandler auditionsChannelHandler;
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() == Config.channelName) {
            voiceApplyReact(event);
        } else if ((event.getChannel().getIdLong() == Config.reportedLines
                || event.getChannel().getIdLong() == Config.acceptedLines
                || event.getChannel().getIdLong() == Config.staffBotChat) && Utils.isAdmin(event.getMember())) {
            LineReportManager.lineReportReact(event);
        }

    }

    private void voiceApplyReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        int numberReacted = Utils.whichNumberWasReacted(event.getEmoji().getName());

        String[] messageArray = message.split("\n");

        String line = messageArray[(2 * numberReacted) - 1];
        String[] splitLine = line.split("=");

        String npcName = splitLine[1].replace(" ", "");

        String questName = messageArray[0].replace("React to apply for a role in", "");
        questName = questName.replace(">>>", "");
        String finalQuestName = questName.replace(" ", "").replace("**", "");

        auditionsChannelHandler.openAudition(finalQuestName, npcName, Objects.requireNonNull(event.getMember()));
    }



    private void sortChannels(Guild guild) {
        if (guild.getCategoryById(Config.applyCategoryId).getChannels().isEmpty()) {
            System.out.println("Tried to sort empty category.");
            return;
        }
        guild.getCategoryById(Config.applyCategoryId)
                .modifyTextChannelPositions()
                .sortOrder(new Comparator<GuildChannel>() {
                    @Override
                    public int compare(GuildChannel o1, GuildChannel o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                }).queue();
    }
}


