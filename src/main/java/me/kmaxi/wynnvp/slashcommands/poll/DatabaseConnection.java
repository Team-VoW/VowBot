package me.kmaxi.wynnvp.slashcommands.poll;

import me.kmaxi.wynnvp.APIKeys;

import java.sql.*;

public class DatabaseConnection {
 //   private static final MySQL mySQL;
    private static final Connection connection;

    static {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://aws.connect.psdb.cloud/vowpolls?sslMode=VERIFY_IDENTITY",
                    APIKeys.sqlUsername,
                    APIKeys.sqlPassword);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        //mySQL = new MySQL(Config.host, Config.port, Config.database, Config.username, APIKeys.sqlPassword);
       // mySQL = new MySQL("66.11.118.47", "3306", "s10530_polls", "u10530_NJoqWZbXbX", "qB^8awM@vlO8Wx+uovrHf2rN");
    }

    // Method to get a database connection
    public static Connection getConnection() throws SQLException {

        return connection;
    }

    public static void runSQLQuery(String query){
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseConnection.getConnection();
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            DatabaseConnection.closeConnection(connection, statement);
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
