package com.nixsolutions.internal;

import com.nixsolutions.exception.ConnectionInvalidateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class OpenSessionConnection {

    private Connection connection;
    private final String host;
    private final String port;
    private final String databaseName;
    private final String user;
    private final String password;

    public OpenSessionConnection(String host, String port, String databaseName, String user, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(buildUrl(host, port, databaseName), user, password);
        this.connection = connection;
        return connection;
    }

    private static String buildUrl(String host, String port, String databaseName) {
        return String.format("jdbc:mysql://%s:%s/%s", host, port, databaseName);
    }

    public void invalidate() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new ConnectionInvalidateException(e.getMessage(), e);
        }
    }
}
