package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import utils.StringUtils;

public class UserService {

    final private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public AuthData registerUser(UserData user) throws CodedException {
        if (StringUtils.isNullOrEmpty(user.username())) {
            throw new CodedException(400, "missing username");
        }
        if (StringUtils.isNullOrEmpty(user.password())) {
            throw new CodedException(400, "missing password");
        }

        try {
            UserData newUser = dataAccess.createUser(user);
            return dataAccess.createAuth(newUser.username());
        } catch (DataAccessException ex) {
            throw new CodedException(403, "Unable to register user");
        }
    }
}
