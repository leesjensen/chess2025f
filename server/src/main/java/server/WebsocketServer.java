package server;

import io.javalin.Javalin;

public class WebsocketServer {
    public WebsocketServer(Javalin server) {
        server.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
                System.out.println("Websocket connected");
            });
            ws.onMessage(ctx -> ctx.send("WebSocket response:" + ctx.message()));
            ws.onClose(ctx -> System.out.println("Websocket closed"));
        });
    }
}
