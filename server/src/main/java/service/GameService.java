package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.Collection;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Collection<GameData> listGames() throws CodedException {
        try {
            return dataAccess.listGames();
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }

    public GameData createGame(String gameName) throws CodedException {
        try {
            return dataAccess.createGame(gameName);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }

}
