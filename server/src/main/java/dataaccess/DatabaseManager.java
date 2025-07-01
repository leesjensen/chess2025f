package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private final DatabaseConfig config;

    public DatabaseManager() throws DataAccessException {
        this(new DatabaseConfig());
    }

    public DatabaseManager(DatabaseConfig config) throws DataAccessException {
        this.config = config;
        createDatabase();
    }

    private void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + config.name;
        try (var conn = DriverManager.getConnection(config.url, config.user, config.password);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    public void deleteDatabase() throws DataAccessException {
        var statement = "DROP DATABASE " + config.name;
        try (var conn = DriverManager.getConnection(config.url, config.user, config.password);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete database", ex);
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(config.url, config.user, config.password);
            conn.setCatalog(config.name);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }
}
