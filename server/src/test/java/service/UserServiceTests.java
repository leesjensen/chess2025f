package service;

import dataaccess.*;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import utils.StringUtils;

public class UserServiceTests extends DbTests {
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void registerUser(DataAccess dataAccess) {
        var service = new UserService(dataAccess);
        var user = randomUser();

        assertDoesNotThrow(() -> {
            var authData = service.registerUser(user);
            assertNotNull(authData);
            assertFalse(StringUtils.isNullOrEmpty(authData.authToken()));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void registerUserDuplicate(DataAccess dataAccess) {
        var service = new UserService(dataAccess);
        var user = randomUser();

        assertDoesNotThrow(() -> service.registerUser(user));
        assertThrows(CodedException.class, () -> service.registerUser(user));
    }
}
