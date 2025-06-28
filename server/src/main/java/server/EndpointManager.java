package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.*;
import service.*;
import dataaccess.*;

import static utils.StringUtils.*;

import java.util.*;

public class EndpointManager {
    private final AdminService adminService;
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public EndpointManager(DataAccess dataAccess) {
        adminService = new AdminService(dataAccess);
        userService = new UserService(dataAccess);
        authService = new AuthService(dataAccess);
        gameService = new GameService(dataAccess);
    }

    public void register(Javalin javalin) {
        javalin.delete("/db", this::clearDb);
        javalin.post("/user", this::registerUser);
        javalin.post("/session", this::loginUser);
        javalin.delete("/session", this::logoutUser);
        javalin.post("/game", this::createGame);
        javalin.get("/game", this::listGames);
        javalin.put("/game", this::joinGame);
    }


    private void clearDb(Context context) throws CodedException {
        adminService.clearApplication();
        context.json("{}");
    }

    private void registerUser(Context context) throws CodedException {
        UserData userData = getBodyObject(context, UserData.class);
        if (isNullOrEmpty(userData.username()) || isNullOrEmpty(userData.email()) || isNullOrEmpty(userData.password())) {
            throw new CodedException(400, "bad request");
        }

        AuthData authData = userService.registerUser(userData);

        var response = Map.of("username", userData.username(), "authToken", authData.authToken());
        context.json(new Gson().toJson(response));
    }


    private void loginUser(Context context) throws CodedException {
        UserData userData = getBodyObject(context, UserData.class);
        if (isNullOrEmpty(userData.username()) || isNullOrEmpty(userData.password())) {
            throw new CodedException(400, "missing required parameters");
        }

        AuthData authData = authService.createSession(userData);

        var response = Map.of("username", userData.username(), "authToken", authData.authToken());
        context.json(new Gson().toJson(response));
    }


    private void logoutUser(Context context) throws CodedException {
        String authToken = context.header("authorization");
        authService.deleteSession(authToken);
        context.json("{}");
    }

    private void createGame(Context context) throws CodedException {
        String authToken = context.header("authorization");
        GameData gameData = getBodyObject(context, GameData.class);
        if (isNullOrEmpty(gameData.gameName())) {
            throw new CodedException(400, "bad request");
        }

        GameData game = gameService.createGame(authToken, gameData.gameName());

        var response = Map.of("gameID", game.gameID());
        context.json(new Gson().toJson(response));
    }

    private void listGames(Context context) throws CodedException {
        String authToken = context.header("authorization");
        Collection<GameData> gameList = gameService.listGames(authToken);

        var response = Map.of("games", gameList);
        context.json(new Gson().toJson(response));
    }

    static class JoinGameReq {
        ChessGame.TeamColor playerColor;
        int gameID;
    }

    private void joinGame(Context context) throws CodedException {
        String authToken = context.header("authorization");
        JoinGameReq joinGameReq = getBodyObject(context, JoinGameReq.class);
        if (joinGameReq.playerColor == null) {
            throw new CodedException(400, "bad request");
        }

        GameData game = gameService.joinGame(authToken, joinGameReq.playerColor, joinGameReq.gameID);

        context.json(new Gson().toJson(game));
    }

    private static <T> T getBodyObject(Context context, Class<T> clazz) {
        var bodyObject = new Gson().fromJson(context.body(), clazz);

        if (bodyObject == null) {
            throw new RuntimeException("missing required body");
        }

        return bodyObject;
    }

}
