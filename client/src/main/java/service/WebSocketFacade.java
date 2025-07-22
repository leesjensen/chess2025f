package service;

import com.google.gson.Gson;

import jakarta.websocket.*;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    MessageObserver responseHandler;

    final MessageObserver defaultObserver = new MessageObserver() {
        public void notify(String message) {
        }

        public void loadGame(GameData game) {
        }
    };

    public WebSocketFacade(String url, MessageObserver messageObserver) throws DeploymentException, IOException, URISyntaxException {
        URI uri = new URI(url);
        URI socketURI = new URI("ws", uri.getUserInfo(), uri.getHost(), uri.getPort(), "/ws", null, null);
        this.responseHandler = messageObserver != null ? messageObserver : defaultObserver;

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);

        this.session.addMessageHandler(new jakarta.websocket.MessageHandler.Whole<String>() {
            public void onMessage(String messageText) {
                ServerMessage message = new Gson().fromJson(messageText, ServerMessage.class);
                switch (message.getServerMessageType()) {
                    case LOAD_GAME -> loadGame(new Gson().fromJson(messageText, LoadMessage.class));
                    case ERROR -> error(message);
                    case NOTIFICATION -> notification(message);
                }
            }
        });
    }


    private void loadGame(LoadMessage message) {
        responseHandler.loadGame(message.game);
    }

    private void error(ServerMessage message) {
        responseHandler.notify(message.toString());
    }

    private void notification(ServerMessage message) {
        responseHandler.notify(message.toString());
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
        session.getBasicRemote().sendText(message);
    }
}

