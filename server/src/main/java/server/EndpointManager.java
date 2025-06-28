package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.*;
import service.*;
import dataaccess.*;

import static utils.StringUtils.*;

import java.util.Map;

public class EndpointManager {
    private final AdminService adminService;
    private final UserService userService;
    private final AuthService authService;

    public EndpointManager(DataAccess dataAccess) {
        adminService = new AdminService(dataAccess);
        userService = new UserService(dataAccess);
        authService = new AuthService(dataAccess);
    }

    public void register(Javalin javalin) {
        javalin.delete("/db", this::clearDb);
        javalin.post("/user", this::registerUser);
        javalin.post("/session", this::loginUser);
        javalin.delete("/session", this::logoutUser);
    }


    private void clearDb(Context context) throws CodedException {
        adminService.clearApplication();
        context.json("{}");
    }

    private void registerUser(Context context) throws CodedException {
        UserData userData = getBodyObject(context, UserData.class);
        if (isNullOrEmpty(userData.username()) || isNullOrEmpty(userData.email()) || isNullOrEmpty(userData.password())) {
            throw new CodedException(400, "missing required parameters");
        }

        AuthData authData = userService.registerUser(userData);

        var response = Map.of("username", userData.username(), "authToken", authData.authToken());
        context.json(new Gson().toJson(response));
    }


    private void loginUser(Context context) throws CodedException {
        UserData userData = getBodyObject(context, UserData.class);
        if (isNullOrEmpty(userData.username()) || isNullOrEmpty(userData.password())) {
            throw new CodedException(400, "missing required parameters");
        }

        AuthData authData = authService.createSession(userData);

        var response = Map.of("username", userData.username(), "authToken", authData.authToken());
        context.json(new Gson().toJson(response));
    }


    private void logoutUser(Context context) throws CodedException {
        String authToken = context.header("authorization");
        authService.deleteSession(authToken);
        context.json("{}");
    }
    
    private static <T> T getBodyObject(Context context, Class<T> clazz) {
        var bodyObject = new Gson().fromJson(context.body(), clazz);

        if (bodyObject == null) {
            throw new RuntimeException("missing required body");
        }

        return bodyObject;
    }

}
