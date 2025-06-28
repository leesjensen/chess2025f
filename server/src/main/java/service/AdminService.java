package service;


import dataaccess.*;

public class AdminService extends Service {
    public AdminService(DataAccess dataAccess) {
        super(dataAccess);
    }

    public void clearApplication() throws CodedException {
        try {
            dataAccess.clear();
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }
}
