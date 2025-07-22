package server;

import io.javalin.websocket.WsContext;
import websocket.messages.*;

public record Connection(int gameID, WsContext ctx) {
    public boolean isOpen() {
        return ctx.session.isOpen();
    }

    public void send(ServerMessage msg) {
        ctx.send(msg.toString());
    }
}