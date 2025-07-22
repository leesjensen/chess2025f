package service;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.*;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final WebSocketFacade webSocket;

    public ServerFacade(String url, MessageObserver messageObserver) throws Exception {
        serverUrl = url;
        httpClient = HttpClient.newHttpClient();
        webSocket = new WebSocketFacade(serverUrl, messageObserver);
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

    public void joinGame(String authToken, int gameID, ChessGame.TeamColor color) throws Exception {
        var request = new JoinGameReq(color, gameID);
        this.makeRequest("PUT", "/game", request, authToken, GameData.class);
        webSocket.connect(authToken, gameID);
    }

    public void observeGame(String authToken, int gameID) throws Exception {
        webSocket.connect(authToken, gameID);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws Exception {
        webSocket.makeMove(authToken, gameID, move);
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
