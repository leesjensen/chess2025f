package dataaccess;

import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;

import java.util.stream.Stream;

import static utils.StringUtils.randomString;

public abstract class DbTests {
    protected static DatabaseManager dbMgr;
    protected static DataAccess db;

    @BeforeAll
    static void createDb() throws Exception {
        dbMgr = new DatabaseManager(new DatabaseConfig("test_chess"));
        db = new MySqlDataAccess(dbMgr);
    }


    @AfterAll
    static void deleteDb() throws Exception {
        dbMgr.deleteDatabase();
    }


    @BeforeEach
    public void ClearDb() throws Exception {
        db.clear();
    }


    protected UserData randomUser() {
        var name = randomString();
        return new UserData(name, "too many secrets", name + "@byu.edu");
    }

    static Stream<Named<DataAccess>> dataAccessImplementations() throws Exception {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess()),
                Named.of("MySqlDataAccess", db)
        );
    }
}
