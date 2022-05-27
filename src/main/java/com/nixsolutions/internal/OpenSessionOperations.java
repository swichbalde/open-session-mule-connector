package com.nixsolutions.internal;

import com.nixsolutions.dto.SessionDetails;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

public class OpenSessionOperations {

    private static final String SELECT_USER_DETAILS_BY_LOGIN_ID =
            "SELECT LOWER(UUID()) as ?, LoginID as ?, AccountNumber as ? " +
                    "FROM ExternalUser " +
                    "WHERE LoginID = ?";
    private static final String SELECT_USER_DETAILS_BY_ACCOUNT_NUMBER =
            "SELECT LOWER(UUID()) as ?, LoginID as ?, AccountNumber as ? " +
                    "FROM ExternalUser " +
                    "WHERE AccountNumber = ?";
    private static final String SESSION_ID_ALIAS = "sessionid";
    private static final String ACTION_BY_ALIAS = "actionby";
    private static final String LOGIN_ID_ALIAS = "loginid";

    private static final String SELECT_OLD_SESSION_ID =
            "SELECT ApplicationSession as ? " +
                    "FROM Security " +
                    "WHERE UserID = ? AND EnvironmentID = ? AND LogoutStatus = ? " +
                    "ORDER BY LoginTime DESC " +
                    "LIMIT 1";
    private static final String INSERT_SESSION_ID =
            "INSERT INTO Security " +
                    "(UserID, UserType, ApplicationSession, LoginTime, LogoutStatus, EnvironmentID) " +
                    "VALUES (?, ?, ?, NOW(), ?, ?)";
    private static final String LOGOUT_STATUS = "LoggedIn";
    private static final Integer USER_TYPE = 1;

    @MediaType(value = ANY, strict = false)
    public String openSessionByLoginId(@Connection OpenSessionConnection connection, String loginId, Long environmentId)
            throws ConnectionException {
        String oldSessionId = getOldSessionId(connection, loginId, environmentId);
        if (oldSessionId != null) {
            return oldSessionId;
        }
        SessionDetails sessionDetails = executeSelectQueryByLoginId(connection, loginId);
        executeInsertQuery(connection, sessionDetails, environmentId);
        return sessionDetails.getSessionId();
    }

    @MediaType(value = ANY, strict = false)
    public String openSessionByAccountNumber(@Connection OpenSessionConnection connection, Long accountNumber,
                                             Long environmentId)
            throws ConnectionException {
        SessionDetails sessionDetails = executeSelectQueryByAccountNumber(connection, accountNumber);
        String oldSessionId = getOldSessionId(connection, sessionDetails.getUserId(), environmentId);
        if (oldSessionId != null) {
            return oldSessionId;
        }
        executeInsertQuery(connection, sessionDetails, environmentId);
        return sessionDetails.getSessionId();
    }

    private String getOldSessionId(OpenSessionConnection connection, String loginId, Long environmentId) throws ConnectionException {
        try (java.sql.Connection jdbcConnection = connection.getConnection()) {
            PreparedStatement preparedStatement = jdbcConnection.prepareStatement(SELECT_OLD_SESSION_ID);
            preparedStatement.setString(1, SESSION_ID_ALIAS);
            preparedStatement.setString(2, loginId);
            preparedStatement.setLong(3, environmentId);
            preparedStatement.setString(4, LOGOUT_STATUS);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(SESSION_ID_ALIAS);
            }
            return null;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    private void executeInsertQuery(OpenSessionConnection connection, SessionDetails sessionDetails, Long environmentId)
            throws ConnectionException {
        try (java.sql.Connection jdbcConnection = connection.getConnection()) {
            PreparedStatement preparedStatement = jdbcConnection.prepareStatement(INSERT_SESSION_ID);
            preparedStatement.setString(1, sessionDetails.getUserId());
            preparedStatement.setInt(2, USER_TYPE);
            preparedStatement.setString(3, sessionDetails.getSessionId());
            preparedStatement.setString(4, LOGOUT_STATUS);
            preparedStatement.setLong(5, environmentId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    private SessionDetails executeSelectQueryByLoginId(OpenSessionConnection connection, String loginId)
            throws ConnectionException {
        try (java.sql.Connection jdbcConnection = connection.getConnection()) {
            PreparedStatement preparedStatement = jdbcConnection.prepareStatement(SELECT_USER_DETAILS_BY_LOGIN_ID);
            preparedStatement.setString(1, SESSION_ID_ALIAS);
            preparedStatement.setString(2, ACTION_BY_ALIAS);
            preparedStatement.setString(3, LOGIN_ID_ALIAS);
            preparedStatement.setString(4, loginId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new SessionDetails(resultSet.getString(SESSION_ID_ALIAS), resultSet.getString(ACTION_BY_ALIAS));
            }
            throw new ConnectionException("User with loginId: " + loginId + " not found");
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    private SessionDetails executeSelectQueryByAccountNumber(OpenSessionConnection connection, Long accountNumber)
            throws ConnectionException {
        try (java.sql.Connection jdbcConnection = connection.getConnection()) {
            PreparedStatement preparedStatement =
                    jdbcConnection.prepareStatement(SELECT_USER_DETAILS_BY_ACCOUNT_NUMBER);
            preparedStatement.setString(1, SESSION_ID_ALIAS);
            preparedStatement.setString(2, ACTION_BY_ALIAS);
            preparedStatement.setString(3, LOGIN_ID_ALIAS);
            preparedStatement.setLong(4, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new SessionDetails(resultSet.getString(SESSION_ID_ALIAS), resultSet.getString(ACTION_BY_ALIAS));
            }
            throw new ConnectionException("User with accountNumber: " + accountNumber + " not found");
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }
}
