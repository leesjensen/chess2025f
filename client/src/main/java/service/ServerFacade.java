package service;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;

    public ServerFacade(String url) {
        serverUrl = url;
        httpClient = HttpClient.newHttpClient();
    }


    public void clear() throws Exception {
        this.makeRequest("DELETE", "/db", null, null, Map.class);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var request = Map.of("username", username, "password", password, "email", email);
        return this.makeRequest("POST", "/user", request, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var request = Map.of("username", username, "password", password);
        return this.makeRequest("POST", "/session", request, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        this.makeRequest("DELETE", "/session", null, authToken, null);
    }

    public GameData createGame(String authToken, String gameName) throws Exception {
        var request = Map.of("gameName", gameName);
        return this.makeRequest("POST", "/game", request, authToken, GameData.class);
    }

    public GameData[] listGames(String authToken) throws Exception {
        record Response(GameData[] games) {
        }
        var response = this.makeRequest("GET", "/game", null, authToken, Response.class);
        return (response != null ? response.games : new GameData[0]);
    }

    public GameData joinGame(String authToken, int gameID, ChessGame.TeamColor color) throws Exception {
        var request = new JoinGameReq(color, gameID);
        this.makeRequest("PUT", "/game", request, authToken, GameData.class);
        return getGame(authToken, gameID);
    }

    private GameData getGame(String authToken, int gameID) throws Exception {
        var games = listGames(authToken);
        for (var game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new Exception("Missing game");
    }

    private <T> T makeRequest(String method, String path, Object requestBody, String authToken, Class<T> clazz) throws Exception {
        try {
            URI uri = new URI(serverUrl + path);
            var requestBuilder = HttpRequest.newBuilder(uri);

            if (authToken != null) {
                requestBuilder.header("Authorization", authToken);
            }

            if (requestBody != null) {
                String json = new Gson().toJson(requestBody);
                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(json));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            var request = requestBuilder.build();
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {
                if (clazz != null) {
                    return new Gson().fromJson(httpResponse.body(), clazz);
                }
                return null;
            }

            var message = (String) (new Gson().fromJson(httpResponse.body(), HashMap.class)).get("message");
            throw new Exception(message);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
