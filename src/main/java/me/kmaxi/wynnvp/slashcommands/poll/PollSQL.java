package me.kmaxi.wynnvp.slashcommands.poll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PollSQL {

    /**
     * Method to create a new poll
     *
     * @param URL Source URL (from casting call club)
     * @param deadline Deadline for voting (as UNIX timestamp)
     * @return int ID of the newly created poll
     */
    public static int createPoll(String URL, int deadline) {
        String dateTime = deadline; //TODO convert the timestamp to "YYYY-MM-DD hh:mm:ss" string
        String addNpcQuery = "INSERT INTO poll(poll_url,poll_deadline) VALUES (" + url + "," + dateTime + ")";
        DatabaseConnection.runSQLQuery(addNpcQuery);
        return 0; //TODO return the ID of the last inserted record
    }

    /**
     * Method to add a NPC to a poll
     *
     * @param npcName The name of the npc
     * @param pollId ID of the poll
     * @throws SQLException Error
     */
    public static void createNpc(String npcName, int pollId) throws SQLException {

        //TODO make this a prepared query
        String addNpcQuery = "INSERT INTO npc(npc_name,npc_poll_id) VALUES (" + npcName + "," + pollId + ")";
        DatabaseConnection.runSQLQuery(addNpcQuery);
    }

    /**
     * Method to add an audition (entry) to a given NPC
     * @param author Name of the voice actor
     * @param npcId ID of the NPC
     * @return int ID of the newly inserted entry
     * @throws SQLException
     */
    public static int createEntry(String author, int npcId) throws SQLException {
        //TODO make this a prepared query
        String addNpcQuery = "INSERT INTO entry(entry_author,entry_npc_id) VALUES (" + author + "," + npcId + ")";
        DatabaseConnection.runSQLQuery(addNpcQuery);
        return 0; //TODO return the ID of the last inserted record
    }

    /**
     * Method returning ID of the most recent poll
     * @return ID of the poll
     */
    public static int getLatestPollId() {
        //TODO execute the following SQL statement:
        //SELECT poll_id FROM poll ORDER BY poll_id DESC LIMIT 1;
        //Parameters: none
        //Return the poll_id returned in the single result
    }

    /**
     * Method to get NPC ID out of its name and ID of the poll it belongs to
     * @param npcName Name of the NPC (as it was passed to PollSQL::createNpc when the poll was being created)
     * @param pollId ID of the poll
     * @return
     */
    public static int getNpcId(String npcName, int pollId) {
        //TODO execute the following SQL statement:
        //SELECT npc_id FROM npc WHERE npc_name = ? AND npc_poll_id = ? LIMIT 1;
        //Parameters: 1: npcName; 2: pollId
        //Return the npc_id returned in the single result
    }

    public static boolean addVote(int entryId, String userId) throws SQLException {
        if (hasVoted(entryId, userId))
            return false;

        VotersSQL.registerVote(userId, entryId);

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String insertQuery = "INSERT INTO vote(vote_entry_id, vote_author) VALUES (?, ?)";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setString(1, entryId);
                stmt.setString(2, userId);
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    result = true;
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return result;
    }


    public static boolean removeVote(int entryId, String userId) throws SQLException {

        VotersSQL.UnregisterVote(userId, entryId);

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String checkQuery = "DELETE FROM vote WHERE vote_entry_id = ? AND vote_author = ? LIMIT 1";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, entryId);
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) { //TODO just check if the DELETE query was excecuted successfully
                    result = true;
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return result;
    }


    public static boolean hasVoted(int entryId, String userId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseConnection.getConnection();
            String selectQuery = "SELECT COUNT(*) FROM vote WHERE vote_entry_id = ? AND vote_author = ?";
            statement = connection.prepareStatement(selectQuery);
            statement.setString(1, entryId);
            statement.setString(2, userId);
            resultSet = statement.executeQuery();
            return resultSet.next(); //TODO return TRUE if the single returned cell contains number higher than 0
        } finally {
            DatabaseConnection.closeConnection(connection, statement, resultSet);
        }
    }
}
