package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import me.kmaxi.wynnvp.services.AuditionsChannelHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Objects;

import static me.kmaxi.wynnvp.BotRegister.guild;

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
            lineReportReact(event);
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




    private static void lineReportReact(MessageReactionAddEvent event) {
        String message = event.retrieveMessage().complete().getContentRaw();

        //If reacted to a message that was not sent from this bot or is not admin
        if (!event.retrieveMessage().complete().getAuthor().getId().equals("821397022250369054")
                || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;

        String line;
        if (event.getChannel().getIdLong() == Config.staffBotChat) {
            line = message;
        } else {
            String[] messageSplitByLine = message.split("\n");

            if (messageSplitByLine.length <= 3) return;

            String str = messageSplitByLine[3];
            line = str.substring(str.indexOf(" ") + 1);
            line = line.replace("`", "");
        }


        String yOrN = switch (event.getReaction().getEmoji().asUnicode().getAsCodepoints()) {
            case "U+2705" -> "y";
            case "U+274c" -> "n";
            case "U+1f399" -> "v";
            case "U+1f5d1" -> "r";
            default -> "none";
        };

        if (yOrN.equals("none")) return;

        sendLineAndDeleteMessage(line, yOrN, event.retrieveMessage().complete(), guild);
    }

    private static void sendLineAndDeleteMessage(String fullLine, String acceptedString, Message message, Guild guild) {

        int responseCode = 0;
        try {
            responseCode = declineOrAcceptLine(fullLine, acceptedString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseCode >= 400) {
            //Failed
            guild.getTextChannelById(Config.staffBotChat).sendMessage("Line: ´" + fullLine + "´ with status **" + acceptedString + "** got response code **" + responseCode + "**").queue();
        } else {
            if (acceptedString.equals("y")) {
                guild.getTextChannelById(Config.acceptedLines).sendMessage(message.getContentRaw()).queue(message1 -> {
                    message1.addReaction(Emoji.fromUnicode(Config.declineUnicode)).queue();
                    message1.addReaction(Emoji.fromUnicode(Config.microphoneUnicode)).queue();
                });
            }

            message.delete().queue();

        }

    }

    private static int declineOrAcceptLine(String fullLine, String acceptedString) throws IOException {

        System.out.println("Line: " + fullLine + " has been marked as " + acceptedString);

        URL url = new URL("https://voicesofwynn.com/api/unvoiced-line-report/resolve");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String data = "line=" + fullLine + "&answer=" + acceptedString + "&apiKey=" + APIKeys.updateApiKey;

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        stream.write(out);

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        http.disconnect();

        return http.getResponseCode();
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


