package me.kmaxi.wynnvp.slashcommands.poll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VotersSQL {
    public static void createVoterTableIfNotExists(String userID){
        String createTableQuery = "CREATE TABLE IF NOT EXISTS _" + userID + " ("
                + "npc VARCHAR(30) PRIMARY KEY,"
                + "votes VARCHAR(200)"
                + ")";
        DatabaseConnection.runSQLQuery(createTableQuery);
    }

    public static void registerVote(String userID, String npcName, String vaName){
        createVoterTableIfNotExists(userID);

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the npc exists in the userIDs table
                String checkQuery = "SELECT votes FROM _" + userID + " WHERE npc = ?";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, npcName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // If the npc exists, update the votes
                    String existingVotes = rs.getString("votes");
                    if (existingVotes == null || existingVotes.isEmpty()) {
                        existingVotes = vaName;
                    } else {
                        existingVotes += "," + vaName;
                    }
                    String updateQuery = "UPDATE _" + userID + " SET votes = ? WHERE npc = ?";
                    stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, existingVotes);
                    stmt.setString(2, npcName);
                    stmt.executeUpdate();
                } else {
                    // If npc does not exist, insert a new row in the  table for the given npc
                    String insertQuery = "INSERT INTO _" + userID + " (npc, votes) VALUES (?, ?)";
                    stmt = conn.prepareStatement(insertQuery);
                    stmt.setString(1, npcName);
                    stmt.setString(2, vaName);
                    stmt.executeUpdate();
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
    }

    public static void UnregisterVote(String userID, String npcName, String vaName){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the npc exists in the userIDs table
                String checkQuery = "SELECT votes FROM _" + userID + " WHERE npc = ?";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, npcName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // If the npc exists, update the votes
                    String existingVotes = rs.getString("votes");
                    if (existingVotes != null && !existingVotes.isEmpty()) {
                        String updateQuery = "UPDATE _" + userID + " SET votes = ? WHERE npc = ?";
                        stmt = conn.prepareStatement(updateQuery);

                        existingVotes = existingVotes.replace(vaName, "");
                        existingVotes = existingVotes.replace(",,", ",");
                        if (existingVotes.contains(",") && existingVotes.lastIndexOf(",") == existingVotes.length() - 1)
                            existingVotes = existingVotes.substring(0, existingVotes.length() - 1);

                        stmt.setString(1, existingVotes);
                        stmt.setString(2, npcName);
                        stmt.executeUpdate();
                    }

                } else {
                    System.out.println("ERROR! The " + npcName + " does not exist in _" + userID + " database");
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
    }

    //A method to get votes for given the userID which is the databaseName and the npcName which is the primary key
    public static String getVotes(String userID, String npcName) {
        Connection conn = null;
        PreparedStatement stmt = null;

        String votes = "";
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the npc exists in the userIDs table
                String checkQuery = "SELECT votes FROM _" + userID + " WHERE npc = ?";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, npcName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    votes = rs.getString("votes");
                } else {
                    System.out.println("ERROR! The " + npcName + " does not exist in _" + userID + " database");
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(conn, stmt);
        }
        return "**" + npcName + "** votes: \n" + votes;
    }

    //A method that prints out everything a user has voted for
    public static String getAllVotes(String userID) {
        Connection conn = null;
        PreparedStatement stmt = null;
        StringBuilder toSend = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the npc exists in the userIDs table
                String checkQuery = "SELECT * FROM _" + userID;
                stmt = conn.prepareStatement(checkQuery);
                ResultSet rs = stmt.executeQuery();
                toSend.append("All Votes:\n ```");
                while (rs.next()) {
                    toSend.append(rs.getString("npc")).append(": ").append(rs.getString("votes")).append("\n");
                }
                toSend.append("```");

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
