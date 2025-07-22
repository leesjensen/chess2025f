package service;

import chess.*;
import dataaccess.*;
import model.*;
import websocket.messages.NotificationMessage;

import java.util.Collection;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class GameService extends Service {

    public GameService(DataAccess dataAccess) {
        super(dataAccess);
    }

    public Collection<GameData> listGames(String authToken) throws CodedException {
        getAuthData(authToken);
        try {
            return dataAccess.listGames();
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    public GameData createGame(String authToken, String gameName) throws CodedException {
        getAuthData(authToken);
        try {
            return dataAccess.createGame(gameName);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    public GameData joinGame(String authToken, ChessGame.TeamColor color, int gameID) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new CodedException(400, "Unknown game");
            } else if (color == null) {
                return gameData;
            } else if (gameData.isGameOver()) {
                throw new CodedException(403, "Game is over");
            } else {
                if (color == ChessGame.TeamColor.WHITE) {
                    if (gameData.whiteUsername() == null || gameData.whiteUsername().equals(username)) {
                        gameData = gameData.setWhite(username);
                    } else {
                        throw new CodedException(403, "Color taken");
                    }
                } else if (color == ChessGame.TeamColor.BLACK) {
                    if (gameData.blackUsername() == null || gameData.blackUsername().equals(username)) {
                        gameData = gameData.setBlack(username);
                    } else {
                        throw new CodedException(403, "Color taken");
                    }
                }
                dataAccess.updateGame(gameData);
            }
            return gameData;
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    public record ConnectionInfo(String username, String role, GameData gameData) {
    }

    public ConnectionInfo connectToGame(String authToken, int gameID) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new CodedException(400, "Unknown game");
            }

            return new ConnectionInfo(username, getRole(username, gameData), gameData);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    public GameData makeMove(String authToken, int gameID, ChessMove move) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new CodedException(400, "Unknown game");
            }

            return dataAccess.updateGame(gameData.makeMove(username, move));
        } catch (InvalidMoveException ex) {
            throw new CodedException(400, ex.getMessage(), ex);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    private String getRole(String username, GameData gameData) {
        if (gameData.whiteUsername().equals(username)) {
            return "white";
        } else if (gameData.blackUsername().equals(username)) {
            return "black";
        }
        return "observer";
    }
}
