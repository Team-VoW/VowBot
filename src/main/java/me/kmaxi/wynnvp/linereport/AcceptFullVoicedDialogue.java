package me.kmaxi.wynnvp.linereport;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AcceptFullVoicedDialogue {


    public static boolean checkIfFullDialogueWasSent(Guild guild, JSONObject jsonObject) {

        String npc = jsonObject.getString("NPC");
        int[] lineDialogueNumbers = getLineNumberAndCount(jsonObject.getString("message"));

        if (lineDialogueNumbers[1] <= 2 || lineDialogueNumbers[0] == 1){
            return false;
        }

        List<Message> messages = getLatestMessages(Objects.requireNonNull(guild.getTextChannelById(Config.reportedLines)), 70);

        List<Message> messageInSameDialogue = new ArrayList<>();
        ArrayList<Boolean> linesReported = new ArrayList<>();
        for (int i = 0; i < lineDialogueNumbers[1]; i++){
            linesReported.add(false);
        }

        for (Message value : Objects.requireNonNull(messages)) {
            String message = value.getContentRaw();
            String[] messageSplitByLine = message.split("\n");

            if (messageSplitByLine.length <= 3) continue;

            for (int j = 0; j < 4; j += 3) {
                String line = messageSplitByLine[j];
                line = line.substring(line.indexOf(" ") + 1);
                line = line.replace("`", "");
                messageSplitByLine[j] = line;
                if (j == 0) {
                    if (!line.equals(npc)) {
                        break;
                    }
                } else if (j == 3) {
                    int[] comparingDialogueN = getLineNumberAndCount(line);
                    if (comparingDialogueN[1] != lineDialogueNumbers[1]) break;
                    if (comparingDialogueN[0] == 1){
                        return false;
                    }
                    messageInSameDialogue.add(value);
                    linesReported.set(comparingDialogueN[0] - 1, true);
                }
            }

        }
        linesReported.set(lineDialogueNumbers[0] - 1, true);
        for (int i = 1; i < linesReported.size(); i++){
            if (!linesReported.get(i)) return false;
        }

        for (int i = 0; i < messageInSameDialogue.size(); i++){
            Message message = messageInSameDialogue.get(i);
            String line = message.getContentRaw();
            try {
                LineReportManager.declineOrAcceptLine(line, "v");
                message.delete().queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            LineReportManager.declineOrAcceptLine(jsonObject.getString("message"), "v");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    private static int[] getLineNumberAndCount(String line) {
        line = line.replaceAll("[\\[\\]]", "");
        line = line.split(" ")[0];
        String[] lineOutOfDialogue = line.split("/");
        int lineNumber = Integer.parseInt(lineOutOfDialogue[0]);
        int lineCount = Integer.parseInt(lineOutOfDialogue[1]);

        return new int[]{lineNumber, lineCount};
    }

    private static List<Message> getLatestMessages(TextChannel channel, int amount) {
        try {
            return channel.getIterableHistory()
                    .takeAsync(amount).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

}
