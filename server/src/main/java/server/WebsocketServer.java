package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import service.CodedException;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadMessage;

public class WebsocketServer {
    private final ConnectionManager connections = new ConnectionManager();
    private final GameService gameService;

    public WebsocketServer(Javalin server, GameService gameService) {
        this.gameService = gameService;
        server.ws("/ws", ws -> {
            ws.onConnect(this::websocketConnect);
            ws.onMessage(this::websocketMessage);
            ws.onClose(this::websocketClose);
        });
    }

    private void websocketConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    private void websocketMessage(WsMessageContext ctx) {
        try {
            var command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> gameConnect(ctx, command);
                case MAKE_MOVE -> makeMove(command);
                case LEAVE -> leaveGame(command);
                case RESIGN -> resignGame(command);
            }
        } catch (Exception ex) {
            System.out.printf("Unhandled ws message: %s", ex.getMessage());
        }
    }

    private void websocketClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void gameConnect(WsContext ctx, UserGameCommand command) throws CodedException {
        var info = gameService.connectToGame(command.getAuthToken(), command.getGameID());
        connections.add(command.getGameID(), ctx);
        connections.broadcast(command.getGameID(), ctx.sessionId(), String.format("%s has joined the game as %s", info.username(), info.role()));
        ctx.send(new LoadMessage(info.gameData()).toString());
    }

    private void makeMove(UserGameCommand command) {
    }

    private void leaveGame(UserGameCommand command) {
    }

    private void resignGame(UserGameCommand command) {
    }
}
