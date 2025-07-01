package service;

import dataaccess.*;
import model.*;

public class AuthService extends Service {
    public AuthService(DataAccess dataAccess) {
        super(dataAccess);
    }

    public AuthData createSession(UserData user) throws CodedException {
        try {
            UserData loggedInUser = dataAccess.getUser(user.username());
            if (loggedInUser != null && loggedInUser.password().equals(user.password())) {
                return dataAccess.createAuth(loggedInUser.username());
            }
            throw new CodedException(401, "Invalid username or password");
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error", ex);
        }
    }

    public void deleteSession(String authToken) throws CodedException {
        try {
            getAuthData(authToken);
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error", ex);
        }
    }
}
