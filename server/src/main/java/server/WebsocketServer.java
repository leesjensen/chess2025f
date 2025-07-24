package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.GameData;
import service.CodedException;
import service.GameService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotificationMessage;

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
                case MAKE_MOVE -> makeMove(ctx, new Gson().fromJson(ctx.message(), MakeMoveCommand.class));
                case LEAVE -> leaveGame(ctx, command);
                case RESIGN -> resignGame(command);
            }
        } catch (Exception ex) {
            var error = new ErrorMessage(ex.getMessage());
            ctx.send(error.toString());
        }
    }

    private void websocketClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void gameConnect(WsContext ctx, UserGameCommand command) throws CodedException {
        var info = gameService.connectToGame(command.getAuthToken(), command.getGameID());
        connections.add(command.getGameID(), ctx);
        var notification = new NotificationMessage(String.format("%s has joined the game as %s", info.username(), info.role()));
        connections.broadcast(command.getGameID(), ctx.sessionId(), notification);
        ctx.send(new LoadMessage(info.gameData()).toString());
    }

    private void makeMove(WsContext ctx, MakeMoveCommand command) throws CodedException {
        var moveInfo = gameService.makeMove(command.getAuthToken(), command.getGameID(), command.getMove());
        var gameData = moveInfo.gameData();
        connections.broadcast(gameData.gameID(), "", new LoadMessage(gameData));

        var moveNotification = new NotificationMessage(String.format("%s moved %s. %s's turn.", moveInfo.username(), command.getMove(), gameData.game().getTeamTurn()));
        connections.broadcast(gameData.gameID(), ctx.sessionId(), moveNotification);

        if (gameData.state() != GameData.State.UNDECIDED) {
            var notification = new NotificationMessage(gameData.description());
            connections.broadcast(gameData.gameID(), "", notification);
        }
    }

    private void leaveGame(WsContext ctx, UserGameCommand command) throws CodedException {
        var username = gameService.leaveGame(command.getAuthToken(), command.getGameID());
        var notification = new NotificationMessage(String.format("%s has left the game", username));
        connections.broadcast(command.getGameID(), ctx.sessionId(), notification);
    }

    private void resignGame(UserGameCommand command) throws CodedException {
        var username = gameService.resignGame(command.getAuthToken(), command.getGameID());
        var notification = new NotificationMessage(String.format("%s has resigned the game", username));
        connections.broadcast(command.getGameID(), "", notification);
    }
}
