package me.kmaxi.wynnvp.slashcommands.poll;

import me.kmaxi.wynnvp.interfaces.SendFunction;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static me.kmaxi.wynnvp.slashcommands.poll.PollSQL.doesTableExist;

public class PollDataFetcher {


    public static void getVotesForPerson(SlashCommandInteractionEvent event){
        event.deferReply().setEphemeral(true).queue();

        String toBePrinted;

        /* NOTE
          This is not an ideal solution, as it doesn't allow the system to work with multiple polls (only with
          the most recent one). However, for the time being, I think it's sufficient.
          To make this better, we could add a argument to the relevant commands, in which the poll ID (or its URL)
          could be set.
        */
        int pollId = PollSQL::getLatestPollId();

        if (event.getOption("npc") == null) {
            toBePrinted = VotersSQL.getAllVotes(event.getUser().getId(), pollId);
        } else {
            npcId = PollSql::getNpcId(Objects.requireNonNull(event.getOption("npc")).getAsString(), pollId)
            toBePrinted = VotersSQL.getVotes(event.getUser().getId(), npcId);
        }
        event.getHook().editOriginal(toBePrinted).queue();

    }
    public static void getAllVotes(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        /* NOTE
          This is not an ideal solution, as it doesn't allow the system to work with multiple polls (only with
          the most recent one). However, for the time being, I think it's sufficient.
          To make this better, we could add a argument to the relevant commands, in which the poll ID (or its URL)
          could be set.
        */
        int pollId = PollSQL::getLatestPollId();

        String npcName = Objects.requireNonNull(event.getOption("npc")).getAsString();
        int npcId = PollSql::getNpcId(Objects.requireNonNull(event.getOption("npc")).getAsString(), pollId)
        getVotes(pollId, ((event1, message) -> event1.getHook().editOriginal(message).queue())); //TODO
    }

    private static void getVotes(int pollId, SendFunction sendFunction) {
        String toSend = VotersSQL::getPollResults(pollId);

        sendFunction.send(event, toSend);
    }
}

