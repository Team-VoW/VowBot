package me.kmaxi.wynnvp.slashcommands.poll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VotersSQL {

    /**
     * Method returning the list of autors of auditions that the given user has chosen for the given NPC
     * @param userID ID of the voice manager
     * @param npcId ID of the NPC
     * @return String to send to the command sendere
     */
    public static String getVotes(String userID, int npcId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        String votes = "";
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the npc exists in the userIDs table
                String checkQuery = "" +
                        "SELECT GROUP_CONCAT(entry.entry_author SEPARATOR '\n- ') AS 'votes'" +
                        "FROM vote" +
                        "JOIN entry ON entry.entry_id = vote.vote_entry_id" +
                        "WHERE vote.vote_author = ? AND entry.npc_id = ?" +
                        "ORDER BY entry.entry_id;";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, userId);
                stmt.setString(2, npcId);
                ResultSet rs = stmt.executeQuery();
                votes = rs.getString("votes");
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return "# Votes for this NPC:\n" + votes;
    }

    /**
     * Method returning the list of autors of auditions that the given user has chosen for all of the NPC in the given poll
     * @param userID ID of the voice manager
     * @param pollId ID of the poll
     * @return String to send to the command sender
     */
    public static String getAllVotes(String userID, int pollId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        StringBuilder toSend = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String checkQuery = "" +
                        "SELECT npc.npc_name AS 'role', entry.entry_author AS 'applicant'" +
                        "FROM vote" +
                        "JOIN entry ON entry.entry_id = vote.vote_entry_id" +
                        "JOIN npc ON npc.npc_id = entry.entry_npc_id" +
                        "WHERE vote.vote_author = ? AND npc.npc_poll_id = ?" +
                        "ORDER BY npc.npc_id, entry.entry_id;";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, userId);
                stmt.setString(2, pollId);
                ResultSet rs = stmt.executeQuery();
                /* TODO
                   The query above will return a table like:
                   |    role    |   applicant   |
                   |------------|---------------|
                   | NPC name 1 | voice actor 3 |
                   | NPC name 1 | voice actor 1 |
                   | NPC name 1 | voice actor 2 |
                   | NPC name 2 | voice actor 1 |
                   | NPC name 2 | voice actor 3 |
                   | etc                        |
                   Somehow process it to output in the correct format, most likely by cycling through the result.
                   The NPCs are sorted – when you get to a row with a new NPC, you can be sure that you've got
                   all the votes for the previous one.
                */
                toSend.append("# All votes in the poll\n");
                while (rs.next()) {
                    /* TODO see above */// toSend.append(rs.getString("npc")).append(": ").append(rs.getString("votes")).append("\n");
                }

                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return toSend.toString();
    }

    /**
     * Workgroup manage/Admin-only method to get all votes in the given poll sorted by characters/NPCs
     * @param pollId ID of the poll to evaluate
     * @return string to send to the command sender
     */
    public static String getPollResults(int pollId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        StringBuilder toSend = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String checkQuery = "" +
                        "SELECT npc.npc_name AS 'role', entry.entry_author AS 'applicant', COUNT(*) AS 'votes'" +
                        "FROM vote" +
                        "JOIN entry ON entry.entry_id = vote.vote_entry_id" +
                        "JOIN npc ON npc.npc_id = entry.entry_npc_id" +
                        "WHERE npc.npc_poll_id = ?" +
                        "GROUP BY npc.npc_name, entry.entry_author" +
                        "ORDER BY npc.npc_id, entry.entry_id;";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, userId);
                stmt.setString(2, pollId);
                ResultSet rs = stmt.executeQuery();
                /* TODO
                   The query above will return a table like:
                   |    role    |   applicant   | votes |
                   |------------|---------------|-------|
                   | NPC name 1 | voice actor 3 |   2   |
                   | NPC name 1 | voice actor 1 |   1   |
                   | NPC name 1 | voice actor 2 |   5   |
                   | NPC name 2 | voice actor 1 |   1   |
                   | NPC name 2 | voice actor 3 |   4   |
                   | etc                                |
                   Somehow process it to output in the correct format, most likely by cycling through the result.
                   The NPCs are sorted – when you get to a row with a new NPC, you can be sure that you've got
                   all the entries for the previous one. Entries with 0 votes do not show.
                */
                toSend.append("# All votes in the poll\n");
                while (rs.next()) {
                    /* TODO see above */// toSend.append(rs.getString("npc")).append(": ").append(rs.getString("votes")).append("\n");
                }

                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return toSend.toString();
    }
}
