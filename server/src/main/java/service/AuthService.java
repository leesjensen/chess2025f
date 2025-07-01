package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService extends Service {
    public AuthService(DataAccess dataAccess) {
        super(dataAccess);
    }

    public AuthData createSession(UserData user) throws CodedException {
        try {
            UserData existingUser = dataAccess.getUser(user.username());
            if (existingUser != null && BCrypt.checkpw(user.password(), existingUser.password())) {
                return dataAccess.createAuth(existingUser.username());
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
