package com.kiddo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiddo.app.models.User;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class StorageManager {
    private static final String PREFS = "kiddo_prefs";
    private static final String USERS_KEY = "kiddo_users";
    private static final String SESSION_KEY = "kiddo_session";
    private static final String DATA_KEY = "kiddo_data";
    private static SharedPreferences prefs;
    private static final Gson gson = new Gson();

    public static void init(Context ctx) {
        if (prefs == null) {
            prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }
    }

    public static Map<String, User> getUsers() {
        String json = prefs.getString(USERS_KEY, null);
        if (json == null) return new HashMap<>();
        Type t = new TypeToken<HashMap<String, User>>() {}.getType();
        return gson.fromJson(json, t);
    }

    public static void saveUsers(Map<String, User> users) {
        prefs.edit().putString(USERS_KEY, gson.toJson(users)).apply();
    }

    public static void saveSession(String userId, boolean remember) {
        Session s = new Session(userId, System.currentTimeMillis(), remember);
        prefs.edit().putString(SESSION_KEY, gson.toJson(s)).apply();
    }

    public static Session getSession() {
        String json = prefs.getString(SESSION_KEY, null);
        if (json == null) return null;
        return gson.fromJson(json, Session.class);
    }

    public static void clearSession() {
        prefs.edit().remove(SESSION_KEY).apply();
    }

    public static String getDataForUser(String userId) {
        return prefs.getString(DATA_KEY + "_" + userId, null);
    }

    public static void saveDataForUser(String userId, String json) {
        prefs.edit().putString(DATA_KEY + "_" + userId, json).apply();
    }

    public static class Session {
        public String userId;
        public long timestamp;
        public boolean remember;
        public Session(String userId, long timestamp, boolean remember) {
            this.userId = userId;
            this.timestamp = timestamp;
            this.remember = remember;
        }
    }
}
