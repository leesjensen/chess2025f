package service;

import dataaccess.*;
import model.*;

public class AuthService {
    private final DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData createSession(UserData user) throws CodedException {
        try {
            UserData loggedInUser = dataAccess.getUser(user.username());
            if (loggedInUser != null && loggedInUser.password().equals(user.password())) {
                return dataAccess.createAuth(loggedInUser.username());
            }
            throw new CodedException(401, "Invalid username or password");
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error");
        }
    }

    public void deleteSession(String authToken) throws CodedException {
        try {
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error");
        }
    }

    public AuthData getAuthData(String authToken) throws CodedException {
        try {
            return dataAccess.getAuth(authToken);
        } catch (DataAccessException ignored) {
            throw new CodedException(500, "Internal server error");
        }
    }
}
