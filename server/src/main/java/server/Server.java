package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.*;
import service.CodedException;

import java.util.Map;

public class Server {

    private Javalin javalin;

    public Server() {
        try {
            javalin = Javalin.create(config -> config.staticFiles.add("web"));

            DataAccess dataAccess = new MySqlDataAccess();
            var endpointManager = new EndpointManager(dataAccess);
            endpointManager.register(javalin);

            new WebsocketServer(javalin);

            javalin.exception(Exception.class, (e, context) -> exceptionHandler(new CodedException(500, e.getMessage()), context));
            javalin.exception(CodedException.class, this::exceptionHandler);
        } catch (DataAccessException ex) {
            System.out.println("Unable to start server " + ex);
        }
    }

    public int run(int desiredPort) {
        if (javalin != null) {
            javalin.start(desiredPort);
            return javalin.port();
        }
        return 0;
    }

    public void stop() {
        if (javalin != null) {
            javalin.stop();
        }
    }

    private void exceptionHandler(CodedException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage())));
        context.status(e.statusCode());
        context.json(body);
    }

}
