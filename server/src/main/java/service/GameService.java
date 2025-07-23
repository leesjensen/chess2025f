package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Collection;


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
        GameData gameData = getGame(gameID);
        if (color == null) {
            return gameData;
        } else if (gameData.isGameOver()) {
            throw new CodedException(403, "Game is over");
        } else {
            if (color == ChessGame.TeamColor.WHITE) {
                if (gameData.whiteUsername() == null || gameData.whiteUsername().equals(username)) {
                    gameData = gameData.setWhite(username);
                    gameData = gameData.setState(GameData.State.UNDECIDED, String.format("%s joined as white", username));
                } else {
                    throw new CodedException(403, "Color taken");
                }
            } else if (color == ChessGame.TeamColor.BLACK) {
                if (gameData.blackUsername() == null || gameData.blackUsername().equals(username)) {
                    gameData = gameData.setBlack(username);
                    gameData = gameData.setState(GameData.State.UNDECIDED, String.format("%s joined as black", username));
                } else {
                    throw new CodedException(403, "Color taken");
                }
            }
            updateGame(gameData);
        }
        return gameData;
    }

    public record ConnectionInfo(String username, String role, GameData gameData) {
    }

    public ConnectionInfo connectToGame(String authToken, int gameID) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        GameData gameData = getGame(gameID);

        return new ConnectionInfo(username, getRole(username, gameData), gameData);
    }

    public GameData makeMove(String authToken, int gameID, ChessMove move) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        try {
            GameData gameData = getGame(gameID);
            return updateGame(gameData.makeMove(username, move));
        } catch (InvalidMoveException ex) {
            throw new CodedException(400, ex.getMessage(), ex);
        }
    }

    public String leaveGame(String authToken, int gameID) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        GameData gameData = getGame(gameID);
        if (!gameData.isGameOver()) {
            if (gameData.whiteUsername().equals(username)) {
                gameData = gameData.setWhite(null);
                gameData = gameData.setState(gameData.state(), String.format("%s playing WHITE has abandoned the game", username));
            } else if (gameData.blackUsername().equals(username)) {
                gameData = gameData.setBlack(null);
                gameData = gameData.setState(gameData.state(), String.format("%s playing BLACK has abandoned the game", username));
            }
            updateGame(gameData);
        }
        return username;
    }


    public String resignGame(String authToken, int gameID) throws CodedException {
        AuthData authData = getAuthData(authToken);
        String username = authData.username();
        GameData gameData = getGame(gameID);
        if (!gameData.isGameOver()) {
            if (username.equals(gameData.whiteUsername()) && gameData.blackUsername() != null) {
                gameData = gameData.setState(GameData.State.BLACK, String.format("%s playing WHITE resigned!", username));
            } else if (username.equals(gameData.blackUsername()) && gameData.whiteUsername() != null) {
                gameData = gameData.setState(GameData.State.WHITE, String.format("%s playing BLACK resigned!", username));
            }
            updateGame(gameData);
        }
        return username;
    }


    private GameData getGame(int gameID) throws CodedException {
        try {
            GameData gameData = dataAccess.getGame(gameID);
            if (gameData == null) {
                throw new CodedException(400, "Unknown game");
            }
            return gameData;
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    private GameData updateGame(GameData gameData) throws CodedException {
        try {
            return dataAccess.updateGame(gameData);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error", ex);
        }
    }

    private String getRole(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return "white";
        } else if (username.equals(gameData.blackUsername())) {
            return "black";
        }
        return "observer";
    }
}
