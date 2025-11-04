package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DbTests;
import model.GameData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class GameServiceTests extends DbTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void CreateGame(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(randomUser());

        var gameService = new GameService(dataAccess);
        GameData game = gameService.createGame(authData.authToken(), "testGame");
        assertEquals("testGame", game.gameName());
        assertTrue(game.gameID() > 0);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void createGameBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.createGame("bogusToken", "testGame"));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void listGames(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(randomUser());

        var gameService = new GameService(dataAccess);
        Collection<GameData> emptyGameList = gameService.listGames(authData.authToken());
        assertEquals(0, emptyGameList.size());

        GameData game1 = gameService.createGame(authData.authToken(), "testGame");
        GameData game2 = gameService.createGame(authData.authToken(), "testGame");

        Collection<GameData> games = gameService.listGames(authData.authToken());
        assertEquals(2, games.size());
        assertTrue(games.contains(game1));
        assertTrue(games.contains(game2));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void listGamesBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.listGames("bogusToken"));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void joinGame(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(randomUser());

        var gameService = new GameService(dataAccess);
        GameData game = gameService.createGame(authData.authToken(), "testGame");
        gameService.joinGame(authData.authToken(), ChessGame.TeamColor.WHITE, game.gameID());

        Collection<GameData> games = gameService.listGames(authData.authToken());
        assertEquals(1, games.size());
        var returnedGame = games.iterator().next();
        assertEquals(game.gameID(), returnedGame.gameID());
        assertEquals(authData.username(), returnedGame.whiteUsername());
        assertEquals("testGame", returnedGame.gameName());
        assertEquals(GameData.State.UNDECIDED, returnedGame.state());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void joinGameBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.joinGame("bogusToken", ChessGame.TeamColor.WHITE, 1));
    }

}
