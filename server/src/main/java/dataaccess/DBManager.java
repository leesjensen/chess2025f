package dataaccess;

import java.sql.Connection;

public interface DBManager {
    public void createDatabase() throws DataAccessException;

    public void clearDatabase() throws DataAccessException;

    public void deleteDatabase() throws DataAccessException;

    public Connection getConnection() throws DataAccessException;

    public String dbName();
}
