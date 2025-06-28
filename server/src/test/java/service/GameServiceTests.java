package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    static Stream<Named<DataAccess>> dataAccessImplementations() {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess())
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void CreateGame(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(new UserData("juan", "too many secrets", "juan@byu.edu"));

        var gameService = new GameService(dataAccess);
        GameData game = gameService.createGame(authData.authToken(), "testGame");
        assertEquals("testGame", game.gameName());
        assertTrue(game.gameID() > 0);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void CreateGameBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.createGame("bogusToken", "testGame"));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void ListGames(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(new UserData("juan", "too many secrets", "juan@byu.edu"));

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
    public void ListGamesBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.listGames("bogusToken"));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void JoinGame(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var authData = userService.registerUser(new UserData("juan", "too many secrets", "juan@byu.edu"));

        var gameService = new GameService(dataAccess);
        GameData game = gameService.createGame(authData.authToken(), "testGame");
        gameService.joinGame(authData.authToken(), ChessGame.TeamColor.WHITE, game.gameID());

        Collection<GameData> games = gameService.listGames(authData.authToken());
        assertEquals(1, games.size());
        var returnedGame = games.iterator().next();
        assertEquals(game.gameID(), returnedGame.gameID());
        assertEquals(returnedGame.whiteUsername(), "juan");
        assertEquals(returnedGame.gameName(), "testGame");
        assertEquals(returnedGame.state(), GameData.State.UNDECIDED);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void JoinGameBadAuthToken(DataAccess dataAccess) {
        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.joinGame("bogusToken", ChessGame.TeamColor.WHITE, 1));
    }

}
