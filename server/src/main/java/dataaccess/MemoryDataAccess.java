package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private int nextID = 1000;

    final private Map<String, UserData> users = new HashMap<>();
    final private Map<Integer, GameData> games = new HashMap<>();
    final private Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
        games.clear();
        auths.clear();
    }

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        if (getUser(user.username()) == null) {
            users.put(user.username(), user);
            return user;
        }
        throw new DataAccessException("attempt to add duplicate user");
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public GameData createGame(String gameName) {
        var gameID = nextID++;
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameData.gameID(), gameData);
        gameData.game().getBoard().resetBoard();
        gameData.game().setTeamTurn(ChessGame.TeamColor.WHITE);
        return gameData;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public GameData updateGame(GameData game) {
        games.put(game.gameID(), game);
        return game;
    }

    @Override
    public AuthData createAuth(String username) {
        var auth = new AuthData(AuthData.generateToken(), username);
        auths.put(auth.authToken(), auth);
        return auth;
    }

    @Override
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }
}
