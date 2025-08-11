package passoff.server;

import chess.ChessGame;
import dataaccess.*;
import org.junit.jupiter.api.*;
import passoff.model.*;
import server.Server;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {

    private static final TestUser TEST_USER = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");

    private static TestServerFacade serverFacade;
    private static DBManager dbManager;

    private static Server server;


    @BeforeAll
    public static void startServer() {
        dbManager = new MySQLDBManager("test_chessPassOffDatabase");
        server = new Server(dbManager);
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
    }

    @BeforeEach
    public void setUp() throws DataAccessException {
        dbManager.clearDatabase();
    }

    @AfterAll
    static void stopServer() throws DataAccessException {
        server.stop();
        dbManager.deleteDatabase();
    }


    @Test
    @DisplayName("Persistence Test")
    @Order(1)
    public void persistenceTest() throws DataAccessException {
        int initialRowCount = getDatabaseRows();

        TestAuthResult regResult = serverFacade.register(TEST_USER);
        String auth = regResult.getAuthToken();

        //create a game
        String gameName = "Test Game";
        TestCreateResult createResult = serverFacade.createGame(new TestCreateRequest(gameName), auth);

        //join the game
        serverFacade.joinPlayer(new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID()), auth);

        Assertions.assertTrue(initialRowCount < getDatabaseRows(), "No new data added to database");

        // Test that we can read the data after a restart
        server.stop();
        startServer();

        //list games using the auth
        TestListResult listResult = serverFacade.listGames(auth);
        Assertions.assertEquals(200, serverFacade.getStatusCode(), "Server response code was not 200 OK");
        Assertions.assertEquals(1, listResult.getGames().length, "Missing game(s) in database after restart");

        TestListEntry game1 = listResult.getGames()[0];
        Assertions.assertEquals(game1.getGameID(), createResult.getGameID());
        Assertions.assertEquals(gameName, game1.getGameName(), "Game name changed after restart");
        Assertions.assertEquals(TEST_USER.getUsername(), game1.getWhiteUsername(),
                "White player username changed after restart");

        //test that we can still log in
        serverFacade.login(TEST_USER);
        Assertions.assertEquals(200, serverFacade.getStatusCode(), "Unable to login");
    }

    @Test
    @DisplayName("Bcrypt")
    @Order(2)
    public void bcrypt() {
        serverFacade.register(TEST_USER);

        executeForAllTables(this::checkTableForPassword);
    }


    @Test
    @DisplayName("Database Error Handling")
    @Order(3)
    public void databaseErrorHandling() {
        var badDbManager = new FailureDBManager(dbManager);
        var server = new Server(badDbManager);
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        var serverFacade = new TestServerFacade("localhost", Integer.toString(port));

        List<Supplier<TestResult>> operations = List.of(
                () -> serverFacade.clear(),
                () -> serverFacade.register(TEST_USER),
                () -> serverFacade.login(TEST_USER),
                () -> serverFacade.logout(UUID.randomUUID().toString()),
                () -> serverFacade.createGame(new TestCreateRequest("inaccessible"), UUID.randomUUID().toString()),
                () -> serverFacade.listGames(UUID.randomUUID().toString()),
                () -> serverFacade.joinPlayer(new TestJoinRequest(ChessGame.TeamColor.WHITE, 1), UUID.randomUUID().toString())
        );

        badDbManager.failing = true;

        try {
            for (Supplier<TestResult> operation : operations) {
                TestResult result = operation.get();
                Assertions.assertEquals(500, serverFacade.getStatusCode(),
                        "Server response code was not 500 Internal Error");
                Assertions.assertNotNull(result.getMessage(), "Invalid Request didn't return an error message");
                Assertions.assertTrue(result.getMessage().toLowerCase(Locale.ROOT).contains("error"),
                        "Error message didn't contain the word \"Error\"");
            }
        } finally {
            server.stop();
        }
    }

    /*
      This class enables the simulation db failure after the server is already running (it started with
      MySQL working normally). If this happens, this should be considered an "Internal Server Error" and the response
      code for any endpoint which no longer can do what it needs to do (which for this project should be all of them)
      should be 500. The body of each of these responses should include a reasonable, relevant error message.
    */
    private static class FailureDBManager implements DBManager {
        private final DBManager dbManager;
        public boolean failing = false;

        public FailureDBManager(DBManager dbManager) {
            this.dbManager = dbManager;
        }

        public void createDatabase() throws DataAccessException {
            dbManager.createDatabase();
        }

        public void clearDatabase() throws DataAccessException {
            dbManager.clearDatabase();
        }

        public void deleteDatabase() throws DataAccessException {
            dbManager.deleteDatabase();
        }

        public Connection getConnection() throws DataAccessException {
            if (failing) {
                throw new DataAccessException(500, "trouble", null);
            }
            return dbManager.getConnection();
        }

        public String dbName() {
            return "failureDB";
        }
    }


    private int getDatabaseRows() {
        AtomicInteger rows = new AtomicInteger();
        executeForAllTables((tableName, connection) -> {
            try (var statement = connection.createStatement()) {
                var sql = "SELECT count(*) FROM " + tableName;
                try (var resultSet = statement.executeQuery(sql)) {
                    if (resultSet.next()) {
                        rows.addAndGet(resultSet.getInt(1));
                    }
                }
            }
        });

        return rows.get();
    }

    private void checkTableForPassword(String table, Connection connection) throws SQLException {
        String sql = "SELECT * FROM " + table;
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columns = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    String value = rs.getString(i);
                    Assertions.assertFalse(value.contains(TEST_USER.getPassword()),
                            "Found clear text password in database");
                }
            }
        }
    }

    private void executeForAllTables(TableAction tableAction) {
        String sql = """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE();
                """;

        try (Connection conn = getConnection(); PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tableAction.execute(resultSet.getString(1), conn);
                }
            }
        } catch (DataAccessException | SQLException e) {
            Assertions.fail(e.getMessage(), e);
        }
    }

    private Connection getConnection() throws DataAccessException {
        return dbManager.getConnection();
    }

    @FunctionalInterface
    private interface TableAction {
        void execute(String tableName, Connection connection) throws SQLException;
    }

}
