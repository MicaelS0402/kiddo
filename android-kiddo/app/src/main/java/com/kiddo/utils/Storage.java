package com.kiddo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Storage {
    private static final String AUTH_STORAGE_KEY = "kiddo_users";
    private static final String SESSION_STORAGE_KEY = "kiddo_session";
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("kiddo_prefs", Context.MODE_PRIVATE);
    }

    public static JSONObject getUsers(Context c) {
        String s = prefs(c).getString(AUTH_STORAGE_KEY, null);
        if (TextUtils.isEmpty(s)) return new JSONObject();
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    public static void saveUsers(Context c, JSONObject users) {
        prefs(c).edit().putString(AUTH_STORAGE_KEY, users.toString()).apply();
    }

    public static void saveSession(Context c, String userId, boolean remember) {
        JSONObject o = new JSONObject();
        try {
            o.put("userId", userId);
            o.put("timestamp", System.currentTimeMillis());
            o.put("remember", remember);
        } catch (JSONException ignored) {}
        prefs(c).edit().putString(SESSION_STORAGE_KEY, o.toString()).apply();
    }

    public static JSONObject checkSession(Context c) {
        String s = prefs(c).getString(SESSION_STORAGE_KEY, null);
        if (TextUtils.isEmpty(s)) return null;
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            return null;
        }
    }

    public static void clearSession(Context c) {
        prefs(c).edit().remove(SESSION_STORAGE_KEY).apply();
    }
}
