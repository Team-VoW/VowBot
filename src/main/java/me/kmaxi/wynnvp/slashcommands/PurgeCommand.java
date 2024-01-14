package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static me.kmaxi.wynnvp.WynnVPBotMain.guild;

public class PurgeCommand {

    public static void trigger(SlashCommandInteractionEvent event) {

        int number = Objects.requireNonNull(event.getOption("amount")).getAsInt();
        if (number > 0 && number <= 100) {
            purgeAmount(number);
        }


    }

    private static void purgeAmount(int amount) {
        Timer timer = new Timer();
        //In 10 seconds
        timer.scheduleAtFixedRate(new SchedulerTask(), 50L, 100L);
    }

    public static class SchedulerTask extends TimerTask {
        private final TextChannel textChannel = guild.getTextChannelById(Config.reportedLines);

        @Override
        public void run() {
            MessageHistory messageHistory = new MessageHistory(textChannel);
            List<Message> msgs;
            msgs = messageHistory.retrievePast(1).complete();
            if (msgs.isEmpty()) {
                guild.getTextChannelById(Config.staffBotChat).sendMessage("Finished clearing.").queue();
                cancel();
                return;
            }
            msgs.get(0).delete().queue();
        }
    }
}
