package model;

import com.google.gson.Gson;

import java.util.UUID;

public record AuthData(String authToken, String username) {
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
