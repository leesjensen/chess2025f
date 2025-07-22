package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.*;
import service.CodedException;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadMessage;
import websocket.messages.ServerMessage;

public class WebsocketServer {
    private final ConnectionManager connections = new ConnectionManager();
    private final GameService gameService;

    public WebsocketServer(Javalin server, GameService gameService) {
        this.gameService = gameService;
        server.ws("/ws", ws -> {
            ws.onConnect(this::onConnect);
            ws.onMessage(this::onMessage);
            ws.onClose(this::onClose);
        });
    }

    private void onConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    private void onMessage(WsMessageContext ctx) {
        try {
            var command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(command);
                case LEAVE -> leave(command);
                case RESIGN -> resign(command);
            }
            ctx.send("hello");
        } catch (Exception ex) {
            System.out.printf("Unhandled ws message: %s", ex.getMessage());
        }
    }

    private void onClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(WsContext ctx, UserGameCommand command) throws CodedException {
        var info = gameService.connectToGame(command.getAuthToken(), command.getGameID());
        connections.add(command.getGameID(), ctx);
        connections.broadcast(command.getGameID(), ctx.sessionId(), String.format("%s has joined the game as %s", info.username(), info.role()));
        ctx.send(new LoadMessage(info.gameData()).toString());
    }

    private void makeMove(UserGameCommand command) {
    }

    private void leave(UserGameCommand command) {
    }

    private void resign(UserGameCommand command) {
    }
}
