package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.*;
import service.CodedException;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final EndpointManager endpointManager;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        DataAccess dataAccess = new MemoryDataAccess();
        endpointManager = new EndpointManager(dataAccess);
        endpointManager.register(javalin);

        javalin.exception(Exception.class, (e, context) -> exceptionHandler(new CodedException(500, e.getMessage()), context));
        javalin.exception(CodedException.class, this::exceptionHandler);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void exceptionHandler(CodedException e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage())));
        context.status(e.statusCode());
        context.json(body);
    }

}
