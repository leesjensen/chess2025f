package server;


import io.javalin.websocket.WsContext;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(int gameID, WsContext ctx) {
        connections.put(ctx.sessionId(), new Connection(gameID, ctx));
    }

    public void remove(WsContext ctx) {
        connections.remove(ctx.sessionId());
    }

    public void broadcast(int gameID, String excludeSessionID, ServerMessage msg) {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.isOpen()) {
                if (c.gameID() == gameID && !c.ctx().sessionId().equals(excludeSessionID)) {
                    c.send(msg);
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.ctx().sessionId());
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("[\n");
        for (var c : connections.values()) {
            sb.append(String.format("  {'game':%d, 'session': %s}%n", c.gameID(), c.ctx().sessionId()));
        }
        sb.append("]");
        return sb.toString();
    }
}
