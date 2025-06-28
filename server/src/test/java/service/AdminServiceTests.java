package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Named;

import static org.junit.jupiter.api.Assertions.*;

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
        gameService.createGame(authData.authToken(), "testGame");

        var service = new AdminService(dataAccess);
        assertDoesNotThrow(service::clearApplication);

        var authService = new AuthService(dataAccess);
        assertThrows(CodedException.class, () -> authService.createSession(user));

        assertThrows(CodedException.class, () -> gameService.listGames(authData.authToken()));
    }
}
