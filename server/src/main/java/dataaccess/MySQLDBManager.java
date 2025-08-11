package dataaccess;

import java.sql.*;
import java.util.*;

public class MySQLDBManager implements DBManager {
    private String databaseName;
    private String dbUsername;
    private String dbPassword;
    private String connectionUrl;

    /**
     * Initialize from the properties file db.properties.
     */
    public MySQLDBManager() {
        loadPropertiesFromResources();
    }

    /**
     * Initialize from the properties file db.properties and overrides the database name. This is useful
     * for testing where you don't want to modify your actual database and need to be able to clean up
     * after the tests.
     */
    public MySQLDBManager(String databaseName) {
        loadPropertiesFromResources();
        this.databaseName = databaseName;
    }

    /**
     * Creates the database if it does not already exist.
     */
    public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)
        ) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Failed to create database: %s", e.getMessage()));
        }
    }

    /**
     * Clears out all content found in the database tables.
     */
    public void clearDatabase() throws DataAccessException {
        try (
                var conn = getConnection();
                var readStmt = conn.createStatement();
                var resultSet = readStmt.executeQuery("SHOW TABLES");
                var writeStmt = conn.createStatement()
        ) {
            while (resultSet.next()) {

                writeStmt.executeUpdate("TRUNCATE TABLE " + resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Failed to clear database: %s", e.getMessage()));
        }
    }


    /**
     * Deletes the database.
     */
    public void deleteDatabase() throws DataAccessException {
        var statement = "DROP DATABASE " + databaseName;
        try (var conn = getConnection(); var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Failed to delete database: %s", e.getMessage()));
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
    public Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }


    public String dbName() {
        return databaseName;
    }

    private void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

}
