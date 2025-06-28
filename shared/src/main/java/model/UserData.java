package model;

import com.google.gson.Gson;

public record UserData(String username, String password, String email) {

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
