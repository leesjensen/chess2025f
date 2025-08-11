package dataaccess;

import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;

import java.util.stream.Stream;

import static utils.StringUtils.randomString;

public abstract class DbTests {
    protected static DataAccess db;
    protected static DBManager connectionManager;

    @BeforeAll
    static void createDb() throws Exception {
        connectionManager = new MySQLDBManager("test_dbTests");
        db = new MySqlDataAccess(connectionManager);
    }


    @AfterAll
    static void deleteDb() throws Exception {
        connectionManager.deleteDatabase();
    }


    @BeforeEach
    public void ClearDb() throws Exception {
        connectionManager.clearDatabase();
    }


    protected UserData randomUser() {
        var name = randomString();
        return new UserData(name, "too many secrets", name + "@byu.edu");
    }

    static Stream<Named<DataAccess>> dataAccessImplementations() {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess()),
                Named.of("MySqlDataAccess", db)
        );
    }
}
