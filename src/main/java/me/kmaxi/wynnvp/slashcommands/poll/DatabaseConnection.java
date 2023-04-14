package me.kmaxi.wynnvp.slashcommands.poll;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    //private static final HikariDataSource dataSource;

    private static MySQL mySQL;

    static {

        mySQL = new MySQL();
/*        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/polls"); // Replace with your database URL
        config.setUsername("root"); // Replace with your database username
        config.setPassword(""); // Replace with your database password
        config.setMaximumPoolSize(5); // Set maximum pool size as per your requirement
        dataSource = new HikariDataSource(config);
        System.out.println("Initialized");*/
    }

    // Method to get a database connection
    public static Connection getConnection() throws SQLException {

        return mySQL.getConnection();

/*        Connection connection = dataSource.getConnection();
        checkConnection(connection);
        return connection;*/
    }

    public static void checkConnection(Connection connection) {
        try {
            if (connection == null) {
                System.out.println("Connection failed. Reconnecting...");
            }
            if (!connection.isValid(3)) {
                System.out.println("Connection is idle or terminated. Reconnecting...");
            }
            if (connection.isClosed()) {
                System.out.println("Connection is closed. Reconnecting...");
            }
        } catch (Exception e) {
            System.out.println("Could not reconnect to database! Error: " + e.getMessage());
        }
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
        closeConnection(connection);
    }

    // Method to close a database connection, and statement
    public static void closeConnection(Connection connection, Statement statement) {
        closeConnection(connection, statement, null);
    }
}
