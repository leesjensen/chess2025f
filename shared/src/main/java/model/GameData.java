package model;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game,
                       State state, String description) {

    public enum State {
        WHITE,
        BLACK,
        DRAW,
        UNDECIDED
    }

    public GameData makeMove(String username, ChessMove move) throws InvalidMoveException {
        if (state != State.UNDECIDED) {
            throw new InvalidMoveException("game already completed");
        }
        game.makeMove(move);

        if (game.isInStalemate(WHITE) || game.isInStalemate(BLACK)) {
            return setState(State.DRAW, "game is a draw");
        } else if (game.isInCheckmate(WHITE)) {
            return setState(State.BLACK, String.format("Black player, %s, won!", blackUsername()));
        } else if (game.isInCheckmate(BLACK)) {
            return setState(State.WHITE, String.format("White player, %s, won!", whiteUsername()));
        } else if (game.isInCheck(WHITE)) {
            return setState(State.UNDECIDED, String.format("White player, %s, is in check!", whiteUsername()));
        } else if (game.isInCheck(BLACK)) {
            return setState(State.UNDECIDED, String.format("Black player, %s, is in check!", blackUsername()));
        }

        return setState(State.UNDECIDED, String.format("%s moved %s. %s's turn.", username, move, game.turn));
    }

    public boolean isGameOver() {
        return state != State.UNDECIDED;
    }

    public GameData setWhite(String username) {
        return new GameData(this.gameID, username, this.blackUsername, this.gameName, this.game, this.state, String.format("%s joined as white", username));
    }

    public GameData setBlack(String username) {
        return new GameData(this.gameID, this.whiteUsername, username, this.gameName, this.game, this.state, String.format("%s joined as black", username));
    }

    public GameData setState(State state, String description) {
        return new GameData(this.gameID, this.whiteUsername, this.blackUsername, this.gameName, this.game, state, description);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String display() {
        return String.format("%s white:%s black:%s state:%s. description: %s", name(gameName), name(whiteUsername), name(blackUsername), state, description);

    }

    private String name(String name) {
        if (name == null) {
            return "NONE";
        }
        return name;
    }

}
