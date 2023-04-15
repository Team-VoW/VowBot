package me.kmaxi.wynnvp.slashcommands.poll;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final MySQL mySQL;

    static {
        //mySQL = new MySQL(Config.host, Config.port, Config.database, Config.username, APIKeys.sqlPassword);
        mySQL = new MySQL("66.11.118.47", "3306", "s10530_polls", "u10530_NJoqWZbXbX", "qB^8awM@vlO8Wx+uovrHf2rN");
    }

    // Method to get a database connection
    public static Connection getConnection() throws SQLException {

        return mySQL.getConnection();
    }

    // Method to close a database connection
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Handle exception, if any
                e.printStackTrace();
            }
        }
    }

    // Method to close a database connection, statement, and result set
    public static void closeConnection(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // Handle exception, if any
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // Handle exception, if any
                e.printStackTrace();
            }
        }
        //closeConnection(connection);
    }

    // Method to close a database connection, and statement
    public static void closeConnection(Connection connection, Statement statement) {
        closeConnection(connection, statement, null);
    }
}
