package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AdminServiceTests {


    static Stream<Named<DataAccess>> dataAccessImplementations() {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void clear(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");
        var authData = userService.registerUser(user);

        var gameService = new GameService(dataAccess);
        gameService.createGame("testGame");

        var service = new AdminService(dataAccess);
        Assertions.assertDoesNotThrow(service::clearApplication);

        var authService = new AuthService(dataAccess);
        Assertions.assertThrows(CodedException.class, () -> authService.createSession(user));
        Assertions.assertNull(authService.getAuthData(authData.authToken()));

        var games = gameService.listGames();
        Assertions.assertEquals(0, games.size());
    }
}
