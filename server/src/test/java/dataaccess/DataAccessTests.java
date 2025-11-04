package dataaccess;

import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class DataAccessTests extends DbTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void writeReadUser(DataAccess dataAccess) throws Exception {
        var user = randomUser();

        Assertions.assertEquals(user, dataAccess.createUser(user));
        Assertions.assertEquals(user, dataAccess.getUser(user.username()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void nullUserName(DataAccess dataAccess) throws Exception {
        var user = new UserData(null, "too many secrets", "null@byu.edu");

        Assertions.assertNull(dataAccess.createUser(user));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void writeReadAuth(DataAccess dataAccess) throws Exception {
        var user = randomUser();

        var authData = dataAccess.createAuth(user.username());
        Assertions.assertEquals(user.username(), authData.username());
        Assertions.assertFalse(authData.authToken().isEmpty());

        var returnedAuthData = dataAccess.getAuth(authData.authToken());
        Assertions.assertEquals(user.username(), returnedAuthData.username());
        Assertions.assertEquals(authData.authToken(), returnedAuthData.authToken());

        var secondAuthData = dataAccess.createAuth(user.username());
        Assertions.assertEquals(user.username(), secondAuthData.username());
        Assertions.assertNotEquals(authData.authToken(), secondAuthData.authToken());
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void deleteAuth(DataAccess dataAccess) throws Exception {
        var user = randomUser();
        var authData = dataAccess.createAuth(user.username());
        dataAccess.deleteAuth(authData.authToken());
        var returnedAuthData = dataAccess.getAuth(authData.authToken());

        Assertions.assertNull(returnedAuthData);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void writeReadGame(DataAccess dataAccess) throws Exception {

        var game = dataAccess.createGame("blitz");
        var updatedGame = game.setBlack("joe");
        dataAccess.updateGame(updatedGame);

        var retrievedGame = dataAccess.getGame(game.gameID());
        Assertions.assertEquals(retrievedGame, updatedGame);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void writeNullGame(DataAccess dataAccess) throws Exception {
        var game = dataAccess.createGame(null);
        Assertions.assertNull(game);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void updateBadGame(DataAccess dataAccess) throws Exception {
        var game = new GameData(-1, null, null, null, null, null, null);
        Assertions.assertThrows(Exception.class, () -> dataAccess.updateGame(game));
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void writeUnknownGame(DataAccess dataAccess) throws Exception {
        var retrievedGame = dataAccess.getGame(100000);
        Assertions.assertNull(retrievedGame);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void listGame(DataAccess dataAccess) throws Exception {

        var games = List.of(dataAccess.createGame("blitz"), dataAccess.createGame("fisher"), dataAccess.createGame("lightning"));
        var returnedGames = dataAccess.listGames();
        Assertions.assertIterableEquals(games, returnedGames);
    }
}
