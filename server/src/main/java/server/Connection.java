package server;

import io.javalin.websocket.WsContext;
import model.GameData;
import websocket.messages.*;

public record Connection(int gameID, WsContext ctx) {
    public boolean isOpen() {
        return ctx.session.isOpen();
    }

    public void sendLoad(GameData gameData) {
        send(new LoadMessage(gameData));
    }

    public void sendNotification(String msg) {
        send(new NotificationMessage(msg));
    }

    public void sendError(String msg) {
        send(new ErrorMessage(String.format("ERROR: %s", msg)));
    }

    private void send(ServerMessage msg) {
        ctx.send(msg.toString());
    }
}