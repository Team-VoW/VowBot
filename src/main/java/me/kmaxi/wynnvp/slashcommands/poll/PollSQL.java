package me.kmaxi.wynnvp.slashcommands.poll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PollSQL {

    // Method to create a new poll table
    public static void createPoll(String pollName) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + pollName + " ("
                    + "messageId VARCHAR(255) PRIMARY KEY,"
                    + "votes INT,"
                    + "userIds VARCHAR(510)"
                    + ")";
            statement = connection.prepareStatement(createTableQuery);
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            DatabaseConnection.closeConnection(connection, statement);
        }
    }

    public static boolean addVote(String pollName, String messageId, String userId) throws SQLException {
        if (!hasVoted(pollName, messageId, userId))
            return false;

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the messageId exists in the polls table for the given pollName
                String checkQuery = "SELECT userIds FROM " + pollName + " WHERE messageId = ?";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, messageId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // If messageId exists, update the poll with the new vote
                    String existingUuid = rs.getString("userIds");
                    if (existingUuid == null || existingUuid.isEmpty()) {
                        existingUuid = userId;
                    } else {
                        existingUuid += "," + userId;
                    }
                    String updateQuery = "UPDATE " + pollName + " SET userIds = ?, votes = votes + 1 WHERE messageId = ?";
                    stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, existingUuid);
                    stmt.setString(2, messageId);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        result = true;
                    }
                } else {
                    // If messageId does not exist, insert a new row in the polls table for the given pollName
                    String insertQuery = "INSERT INTO " + pollName + " (messageId, userIds, votes) VALUES (?, ?, 1)";
                    stmt = conn.prepareStatement(insertQuery);
                    stmt.setString(1, messageId);
                    stmt.setString(2, userId);
                    int rowsInserted = stmt.executeUpdate();
                    if (rowsInserted > 0) {
                        result = true;
                    }
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


    public static boolean removeVote(String pollName, String messageId, String userId) throws SQLException {

        if (!hasVoted(pollName, messageId, userId))
            return false;

        Connection conn = null;
        PreparedStatement stmt = null;
        boolean result = false;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Check if the messageId exists in the polls table for the given pollName
                String checkQuery = "SELECT userIds, votes FROM " + pollName + " WHERE messageId = ?";
                stmt = conn.prepareStatement(checkQuery);
                stmt.setString(1, messageId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // If messageId exists, update the poll by subtracting one vote and removing the userId from uuids
                    String existingUuid = rs.getString("userIds");
                    int votes = rs.getInt("votes");
                    if (existingUuid != null && !existingUuid.isEmpty() && existingUuid.contains(userId)) {
                        existingUuid = existingUuid.replace(userId, "");
                        existingUuid = existingUuid.replaceAll(",,", ",");
                        existingUuid = existingUuid.replaceAll("^,", "");
                        existingUuid = existingUuid.replaceAll(",$", "");
                        if (existingUuid.isEmpty()) {
                            existingUuid = null;
                        }
                    }
                    String updateQuery = "UPDATE " + pollName + " SET userIds = ?, votes = ? WHERE messageId = ?";
                    stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, existingUuid);
                    stmt.setInt(2, votes - 1);
                    stmt.setString(3, messageId);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        result = true;
                    }
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



    // Method to check if a user has voted in a poll
    public static boolean hasVoted(String pollName, String messageId, String userId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseConnection.getConnection();
            String selectQuery = "SELECT * FROM " + pollName + " WHERE messageId = ? AND userIds LIKE ?";
            statement = connection.prepareStatement(selectQuery);
            statement.setString(1, messageId);
            statement.setString(2, "%" + userId + "%");
            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            DatabaseConnection.closeConnection(connection, statement, resultSet);
        }
    }
}
