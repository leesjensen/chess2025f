package client;

import chess.ChessGame;
import dataaccess.DBManager;
import dataaccess.DataAccessException;
import dataaccess.MySQLDBManager;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import server.Server;
import service.ServerFacade;
import utils.StringUtils;

import java.util.Arrays;

import static utils.StringUtils.randomString;


public class ServerFacadeTests {

    private static Server server;
    private static DBManager dbManager;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() throws Exception {
        dbManager = new MySQLDBManager("test_serverFacade");
        server = new Server(dbManager);
        var port = server.run(0);
        serverFacade = new ServerFacade(String.format("http://localhost:%d", port), null);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterEach
    public void clearDb() throws DataAccessException {
//        dbManager.clearDatabase();
    }

    @AfterAll
    static void stopServer() throws DataAccessException {
        server.stop();
        dbManager.deleteDatabase();
    }


    @Test
    public void register() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        assertEquals(user.username(), authData.username());
    }

    @Test
    public void registerNoUsername() {
        UserData user = randomUser();
        assertThrows(Exception.class, () -> serverFacade.register("", user.password(), user.email()));
    }

    @Test
    public void login() throws Exception {
        UserData user = randomUser();
        serverFacade.register(user.username(), user.password(), user.email());
        AuthData authData = serverFacade.login(user.username(), user.password());
        assertEquals(user.username(), authData.username());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        UserData user = randomUser();
        serverFacade.register(user.username(), user.password(), user.email());
        assertThrows(Exception.class, () -> serverFacade.login(user.username(), "bogusPassword"));
    }


    @Test
    public void logout() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        serverFacade.logout(authData.authToken());
        assertThrows(Exception.class, () -> serverFacade.createGame(authData.authToken(), "game name"));
    }


    @Test
    public void logoutBadAuth() {
        assertThrows(Exception.class, () -> serverFacade.logout("bogusToken"));
    }


    @Test
    public void createGame() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        var gameName = StringUtils.randomString();
        GameData game = serverFacade.createGame(authData.authToken(), gameName);
        assertTrue(game.gameID() != 0);
    }


    @Test
    public void createGameBadAuth() {
        assertThrows(Exception.class, () -> serverFacade.createGame("bogusToken", "game name"));
    }


    @Test
    public void listGames() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        var games = serverFacade.listGames(authData.authToken());
        assertEquals(0, games.length);

        var gameName = StringUtils.randomString();
        serverFacade.createGame(authData.authToken(), gameName);
        var updatedGames = serverFacade.listGames(authData.authToken());
        assertEquals(1, updatedGames.length);
        assertEquals(gameName, updatedGames[0].gameName());
    }


    @Test
    public void listGamesBadAuth() {
        assertThrows(Exception.class, () -> serverFacade.listGames("bogusToken"));
    }


    @Test
    public void joinGame() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        var gameName = StringUtils.randomString();
        var game = serverFacade.createGame(authData.authToken(), gameName);
        serverFacade.joinGame(authData.authToken(), game.gameID(), ChessGame.TeamColor.WHITE);
        var games = serverFacade.listGames(authData.authToken());

        var joinedGame = Arrays.stream(games)
                .filter(g -> g.gameID() == game.gameID())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created game not found in listGames"));

        assertEquals(game.gameID(), joinedGame.gameID());
        assertEquals(user.username(), joinedGame.whiteUsername());
    }

    @Test
    public void joinGameNoColor() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        var gameName = StringUtils.randomString();
        var game = serverFacade.createGame(authData.authToken(), gameName);
        assertThrows(Exception.class, () -> serverFacade.joinGame(authData.authToken(), game.gameID(), null));
    }

    @Test
    public void joinGameBadAuth() {
        assertThrows(Exception.class, () -> serverFacade.joinGame("bogusAuth", 3, ChessGame.TeamColor.WHITE));
    }

    @Test
    public void joinGameBadID() throws Exception {
        UserData user = randomUser();
        AuthData authData = serverFacade.register(user.username(), user.password(), user.email());
        assertThrows(Exception.class, () -> serverFacade.joinGame(authData.authToken(), 3, ChessGame.TeamColor.WHITE));
    }


    protected UserData randomUser() {
        var name = randomString();
        return new UserData(name, "too many secrets", name + "@byu.edu");
    }

}
