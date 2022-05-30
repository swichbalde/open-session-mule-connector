package com.nixsolutions.internal;

import com.nixsolutions.exception.ConnectionInvalidateException;
import java.sql.SQLException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSessionConnectionProvider
    implements PoolingConnectionProvider<OpenSessionConnection> {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private final Logger LOGGER = LoggerFactory.getLogger(OpenSessionConnectionProvider.class);

    @Parameter
    @DisplayName("Host")
    private String host;

    @Parameter
    @DisplayName("Port")
    private String port;

    @Parameter
    @DisplayName("Database")
    private String databaseName;

    @Parameter
    @DisplayName("User")
    private String user;

    @Parameter
    @DisplayName("Password")
    private String password;

    @Override
    public OpenSessionConnection connect() throws ConnectionException {
        try {
            Class.forName(JDBC_DRIVER);
            return new OpenSessionConnection(host, port, databaseName, user, password);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error while connecting: " + e.getMessage(), e);
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect(OpenSessionConnection connection) {
        try {
            connection.invalidate();
        } catch (ConnectionInvalidateException e) {
            LOGGER.error("Error while disconnecting: " + e.getMessage(), e);
        }
    }

    @Override
    public ConnectionValidationResult validate(OpenSessionConnection connection) {
        try {
            connection.getConnection();
            return ConnectionValidationResult.success();
        } catch (SQLException e) {
            return ConnectionValidationResult.failure(e.getMessage(), e);
        }
    }
}
