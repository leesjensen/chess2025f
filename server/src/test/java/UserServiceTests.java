import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import service.CodedException;
import service.UserService;
import utils.StringUtils;

import java.util.stream.Stream;

public class UserServiceTests {

    static Stream<Named<DataAccess>> dataAccessImplementations() {
        return Stream.of(
                Named.of("MemoryDataAccess", new MemoryDataAccess())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void registerUser(DataAccess dataAccess) {
        var service = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");

        Assertions.assertDoesNotThrow(() -> {
            var authData = service.registerUser(user);
            Assertions.assertNotNull(authData);
            Assertions.assertFalse(StringUtils.isNullOrEmpty(authData.authToken()));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataAccessImplementations")
    public void registerUserDuplicate(DataAccess dataAccess) {
        var service = new UserService(dataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");

        Assertions.assertDoesNotThrow(() -> service.registerUser(user));
        Assertions.assertThrows(CodedException.class, () -> service.registerUser(user));
    }
}
