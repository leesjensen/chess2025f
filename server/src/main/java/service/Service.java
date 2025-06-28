package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;

public abstract class Service {
    protected final DataAccess dataAccess;

    protected Service(DataAccess dataAccess) {

        this.dataAccess = dataAccess;
    }


    protected AuthData getAuthData(String authToken) throws CodedException {
        try {
            if (authToken != null) {
                var authData = dataAccess.getAuth(authToken);
                if (authData != null) {
                    return authData;
                }
            }

            throw new CodedException(401, "Not authorized");
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Internal server error");
        }
    }

}
