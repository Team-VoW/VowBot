package me.kmaxi.wynnvp.slashcommands.poll;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;


    public MySQL(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    public MySQL() {
        this.host  = "localhost";
        this.port = "3306";
        this.database = "polls";
        this.username = "root";
        this.password = "";
    }

    public boolean isConnected() {
        return (connection != null);
    }

    public void connect() throws SQLException, ClassNotFoundException {
        if (isConnected()) disconnect();


        System.out.println("jdbc:mysql://" +
                host + ":" + port + "/" + database + "?useSSL=false" + username + password);

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" +
                host + ":" + port + "/" + database + "?useSSL=false", username, password);

    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        checkConnection();
        return connection;
    }

    public void checkConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            if (connection == null) {
                System.out.println("Connection failed. Reconnecting...");
                reconnect();
            }
            if (!connection.isValid(3)) {
                System.out.println("Connection is idle or terminated. Reconnecting...");
                reconnect();
            }
            if (connection.isClosed()) {
                System.out.println("Connection is closed. Reconnecting...");
                reconnect();
            }
        } catch (Exception e) {
            System.out.println("Could not reconnect to database! Error: " + e.getMessage());
        }
    }

    private void reconnect() {
        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


}
