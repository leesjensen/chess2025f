package server;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import model.*;
import service.*;
import dataaccess.*;

import java.util.Map;

public class EndpointManager {
    private final AdminService adminService;
    private final UserService userService;

    public EndpointManager(DataAccess dataAccess) {
        adminService = new AdminService(dataAccess);
        userService = new UserService(dataAccess);
    }

    public void register(Javalin javalin) {
        javalin.delete("/db", this::clearDb);
        javalin.post("/user", this::registerUser);
    }


    private void clearDb(Context context) throws CodedException {
        adminService.clearApplication();
        context.json("{}");
    }

    private void registerUser(Context context) throws CodedException {
        UserData userData = getBodyObject(context, UserData.class);
        if (utils.StringUtils.isNullOrEmpty(userData.username()) || utils.StringUtils.isNullOrEmpty(userData.email()) || utils.StringUtils.isNullOrEmpty(userData.password())) {
            throw new CodedException(400, "missing required parameters");
        }

        AuthData authData = userService.registerUser(userData);

        var response = Map.of("username", userData.username(), "authToken", authData.authToken());
        context.json(new Gson().toJson(response));
    }


    private static <T> T getBodyObject(Context context, Class<T> clazz) {
        var bodyObject = new Gson().fromJson(context.body(), clazz);

        if (bodyObject == null) {
            throw new RuntimeException("missing required body");
        }

        return bodyObject;
    }
}
