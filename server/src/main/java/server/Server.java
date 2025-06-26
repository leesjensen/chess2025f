package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.*;
import model.UserData;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.delete("/db", this::clearDb);
        javalin.post("/user", this::registerUser);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    private void clearDb(Context context) {
        context.json("{}");
    }

    private void registerUser(Context context) {
        UserData userData = getBodyObject(context, UserData.class);
        var response = Map.of("username", userData.username(), "authToken", "xxx");
        context.json(new Gson().toJson(response));
    }

    private static <T> T getBodyObject(Context context, Class<T> clazz) {
        var bodyObject = new Gson().fromJson(context.body(), clazz);

        if (bodyObject == null) {
            throw new RuntimeException("missing required body");
        }

        return bodyObject;
    }

    public void stop() {
        javalin.stop();
    }
}
