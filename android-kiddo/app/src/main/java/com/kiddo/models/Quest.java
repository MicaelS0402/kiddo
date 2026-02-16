package com.kiddo.models;

public class Quest {
    public String id;
    public String text;
    public int xp;

    public Quest() {}
    public Quest(String id, String text, int xp) {
        this.id = id;
        this.text = text;
        this.xp = xp;
    }
}
