package me.kmaxi.wynnvp.linereport;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AcceptFullVoicedDialogue {


    public static void queueCheckIfDialogueWasSent(Guild guild, JSONObject jsonObject) {

        String line = jsonObject.getString("message");

        int[] lineDialogueNumbers = getLineNumberAndCount(line);

        if (lineDialogueNumbers[1] <= 2 || lineDialogueNumbers[0] == 1 || lineDialogueNumbers[0] != lineDialogueNumbers[1]) {
            return;
        }
        String npc = jsonObject.getString("NPC");
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                checkIfFullDialogueWasSent(guild, line, npc);
            }
        }, 8000);

    }

    private static void checkIfFullDialogueWasSent(Guild guild, String checkLine, String npc) {

        int[] lineDialogueNumbers = getLineNumberAndCount(checkLine);


        List<Message> messages = getLatestMessages(Objects.requireNonNull(guild.getTextChannelById(Config.reportedLines)), 70);

        List<Message> messageInSameDialogue = new ArrayList<>();
        List<String> stringInSameDialogue = new ArrayList<>();
        ArrayList<Boolean> linesReported = new ArrayList<>();
        for (int i = 0; i < lineDialogueNumbers[1]; i++) {
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
                    if (comparingDialogueN[0] == 1) {
                        return;
                    }
                    messageInSameDialogue.add(value);
                    stringInSameDialogue.add(line);
                    linesReported.set(comparingDialogueN[0] - 1, true);
                }
            }

        }


        for (int i = 1; i < linesReported.size(); i++) {
            System.out.println(linesReported.get(i));
            if (!linesReported.get(i)) return;
        }

        for (int i = 0; i < messageInSameDialogue.size(); i++) {
            Message message = messageInSameDialogue.get(i);
            String line = stringInSameDialogue.get(i);
            LineReportManager.sendLineAndDeleteMessage(line, "v", message, guild);

        }

    }


    private static int[] getLineNumberAndCount(String line) {
        line = line.replaceAll("[\\[\\]]", "");
        line = line.split(" ")[0];
        String[] lineOutOfDialogue = line.split("/");
        int lineNumber = 0;
        int lineCount = 0;
        if (lineOutOfDialogue[0].matches("-?\\d+")
                && lineOutOfDialogue[1].matches("-?\\d+")){
             lineNumber = Integer.parseInt(lineOutOfDialogue[0]);
             lineCount = Integer.parseInt(lineOutOfDialogue[1]);
        }
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
