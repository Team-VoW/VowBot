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
    public static void getSingleNpc(SlashCommandInteractionEvent event) {

        event.deferReply().setEphemeral(true).queue();

        String npcName = Objects.requireNonNull(event.getOption("npc")).getAsString();
        getVotes(npcName, event, ((event1, message) -> event1.getHook().editOriginal(message).queue()));
    }

    private static void getVotes(String tableName, SlashCommandInteractionEvent event, SendFunction sendFunction) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if table exists in the database
                if (doesTableExist(tableName)) {
                    String query = "SELECT messageId, votes, userIds FROM " + tableName;
                    stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    StringBuilder toSend = new StringBuilder();
                    toSend.append("Votes for: **").append(tableName).append("**:");
                    toSend.append("```");

                    while (rs.next()) {
                        String messageID = rs.getString("messageId");
                        int voteCount = rs.getInt("votes");
                        String voteUUIDs = rs.getString("userIds");
                        toSend.append("\n").append(messageID).append(": ").append(voteCount).append(" | ");

                        if (voteUUIDs == null){
                            continue;
                        }

                        for (String uuid : voteUUIDs.split("-")) {
                            Objects.requireNonNull(event.getGuild()).retrieveMemberById(uuid).queue(member -> {
                                // use member here
                                toSend.append(member.getEffectiveName()).append(", ");
                            });
                        }
                    }
                    toSend.append("```");

                    sendFunction.send(event, toSend.toString());

                    rs.close();
                    stmt.close();
                } else {
                    sendFunction.send(event, "Table does not exist");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
    }


}
