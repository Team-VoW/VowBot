package me.kmaxi.wynnvp.commands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PurgeCommand {

    public static void trigger(String[] splitMessage) {
        if (splitMessage.length == 1) {
            purgeAmount(100);
            //If it is a number
        } else if (splitMessage[1].matches("-?(0|[1-9]\\d*)")) {

            int number = Integer.parseInt(splitMessage[1]);
            if (number > 0 && number <= 100) {
                purgeAmount(number);
            }

        }
    }


    private static void purgeAmount(int amount) {
        Timer timer = new Timer();
        //In 10 seconds
        timer.scheduleAtFixedRate(new SchedulerTask(), 100L, 100L);

    }

    public static class SchedulerTask extends TimerTask {
        private final TextChannel textChannel = LineReportManager.guild.getTextChannelById(Config.reportedLines);

        @Override
        public void run() {
            MessageHistory messageHistory = new MessageHistory(textChannel);
            List<Message> msgs;
            msgs = messageHistory.retrievePast(1).complete();
            if (msgs.isEmpty()){
                LineReportManager.guild.getTextChannelById(Config.staffBotChat).sendMessage("Finished clearing.").queue();
                cancel();
                return;
            }
            msgs.get(0).delete().queue();
        }
    }
}
