package service;

import com.google.gson.Gson;

import jakarta.websocket.*;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class WebSocketFacade extends Endpoint {

    Session session;
    DisplayHandler responseHandler;


    public WebSocketFacade(String url, DisplayHandler responseHandler) throws DeploymentException, IOException, URISyntaxException {
        URI uri = new URI(url);
        URI socketURI = new URI("ws", uri.getUserInfo(), uri.getHost(), uri.getPort(), "/ws", null, null);
        this.responseHandler = responseHandler;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                responseHandler.process(message);
            }
        });
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
    }

    public void sendCommand(UserGameCommand command) throws IOException {
        sendMessage(command.toString());
    }

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(Map.of("message", message)));
    }
}

