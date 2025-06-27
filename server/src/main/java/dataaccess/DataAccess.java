package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    void clear();

    UserData createUser(UserData user);

    UserData getUser(String username);

    GameData createGame(GameData game);

    GameData getGame(String gameID);

    Collection<GameData> listGames();

    GameData updateGame(GameData game);

    AuthData createAuth(AuthData auth);

    AuthData getAuth(String authToken);

    void deleteAuth(String authToken);
}
