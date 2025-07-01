package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

public class UserService extends Service {
    public UserService(DataAccess dataAccess) {
        super(dataAccess);
    }


    public AuthData registerUser(UserData user) throws CodedException {
        try {
            UserData newUser = dataAccess.createUser(user);
            return dataAccess.createAuth(newUser.username());
        } catch (DataAccessException ex) {
            throw new CodedException(403, "Unable to register user", ex);
        }
    }
}
