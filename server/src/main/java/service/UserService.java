package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserService extends Service {
    public UserService(DataAccess dataAccess) {
        super(dataAccess);
    }


    public AuthData registerUser(UserData user) throws CodedException {
        try {
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            var encryptedUser = new UserData(user.username(), hashedPassword, user.email());
            UserData newUser = dataAccess.createUser(encryptedUser);
            return dataAccess.createAuth(newUser.username());
        } catch (DataAccessException ex) {
            var statusCode = ex.statusCode() != 0 ? ex.statusCode() : 500;
            throw new CodedException(statusCode, "Unable to register user", ex);
        }
    }
}
