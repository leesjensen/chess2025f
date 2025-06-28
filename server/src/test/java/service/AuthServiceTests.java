package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import utils.StringUtils;

import java.util.stream.Stream;

public class AuthServiceTests {

    static Stream<Named<DataAccess>> dataAccessImplementations() {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void login(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");
        userService.registerUser(user);

        var authService = new AuthService(dataAccess);

        assertDoesNotThrow(() -> {
            var authData = authService.createSession(user);
            assertNotNull(authData);
            assertFalse(StringUtils.isNullOrEmpty(authData.authToken()));
        });

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void loginBadPassword(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");
        userService.registerUser(user);

        var authService = new AuthService(dataAccess);

        assertThrows(CodedException.class, () -> {
            var badUser = new UserData("juan", "", "juan@byu.edu");
            var authData = authService.createSession(badUser);
            assertNotNull(authData);
            assertFalse(StringUtils.isNullOrEmpty(authData.authToken()));
        });

    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void logout(DataAccess dataAccess) throws Exception {
        var userService = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");
        var authData = userService.registerUser(user);

        var authService = new AuthService(dataAccess);

        assertDoesNotThrow(() -> authService.deleteSession(authData.authToken()));

        var gameService = new GameService(dataAccess);
        assertThrows(CodedException.class, () -> gameService.listGames(authData.authToken()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void logoutBadAuthToken(DataAccess dataAccess) {
        var authService = new AuthService(dataAccess);
        assertThrows(CodedException.class, () -> authService.deleteSession("bogusToken"));

    }

}
