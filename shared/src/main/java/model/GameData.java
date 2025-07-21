package model;

import chess.ChessGame;
import com.google.gson.Gson;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game,
                       State state) {

    public enum State {
        WHITE,
        BLACK,
        DRAW,
        UNDECIDED
    }

    public boolean isGameOver() {
        return state != State.UNDECIDED;
    }

    public GameData setWhite(String userName) {
        return new GameData(this.gameID, userName, this.blackUsername, this.gameName, this.game, this.state);
    }

    public GameData setBlack(String userName) {
        return new GameData(this.gameID, this.whiteUsername, userName, this.gameName, this.game, this.state);
    }

    public GameData setState(State state) {
        return new GameData(this.gameID, this.whiteUsername, this.blackUsername, this.gameName, this.game, state);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String display() {
        return String.format("%s white:%s black:%s state:%s", name(gameName), name(whiteUsername), name(blackUsername), state);

    }

    private String name(String name) {
        if (name == null) {
            return "NONE";
        }
        return name;
    }
}
