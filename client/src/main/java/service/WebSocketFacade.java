package service;

import com.google.gson.Gson;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;

public class WebSocketFacade extends Endpoint {

    Session session;
    DisplayHandler responseHandler;

    public static void main(String[] args) throws Exception {
        var f = new WebSocketFacade("ws://localhost:8080/ws", System.out::println);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a message you want to echo:");
        while (true) {
            f.sendCommand(scanner.nextLine());
        }
    }

    public WebSocketFacade(String url, DisplayHandler responseHandler) throws DeploymentException, IOException, URISyntaxException {
        URI socketURI = new URI(url);
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

    public void sendCommand(String message) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(Map.of("message", message)));
    }
}

