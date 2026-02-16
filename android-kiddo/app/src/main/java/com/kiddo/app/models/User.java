package com.kiddo.app.models;

public class User {
    public String name;
    public String email;
    public String passwordHash;
    public String avatar;

    public User() {}

    public User(String name, String email, String passwordHash, String avatar) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatar = avatar;
    }
}
