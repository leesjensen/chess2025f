package service;

import model.GameData;

public interface MessageObserver {
    void notify(String message);

    void loadGame(GameData game);
}
