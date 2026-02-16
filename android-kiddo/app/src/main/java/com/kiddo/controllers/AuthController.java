package com.kiddo.controllers;

import android.content.Context;

import com.kiddo.models.User;
import com.kiddo.utils.Hash;
import com.kiddo.utils.Storage;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthController {
    public static String register(Context c, String name, String emailOrUser, String password) throws Exception {
        if (name == null || name.trim().length() < 2) throw new Exception("Nome muito curto");
        if (emailOrUser == null || emailOrUser.trim().length() < 3) throw new Exception("E-mail/usuário muito curto");
        if (password == null || password.length() < 4) throw new Exception("Senha deve ter no mínimo 4 caracteres");
        String email = emailOrUser.trim().toLowerCase();
        JSONObject users = Storage.getUsers(c);
        if (users.has(email)) throw new Exception("Este e-mail/usuário já está cadastrado");
        JSONObject u = new JSONObject();
        u.put("name", name);
        u.put("email", email);
        u.put("passwordHash", Hash.hashPassword(password));
        JSONObject data = new JSONObject();
        data.put("progress", new JSONObject());
        data.put("totalXP", 0);
        data.put("streakCount", 0);
        data.put("lastCompletedDate", JSONObject.NULL);
        data.put("customQuests", null);
        u.put("data", data);
        users.put(email, u);
        Storage.saveUsers(c, users);
        Storage.saveSession(c, email, false);
        return email;
    }

    public static String login(Context c, String emailOrUser, String password, boolean remember) throws Exception {
        String email = emailOrUser.trim().toLowerCase();
        JSONObject users = Storage.getUsers(c);
        if (!users.has(email)) throw new Exception("Usuário não encontrado");
        JSONObject u = users.getJSONObject(email);
        String hash = Hash.hashPassword(password);
        if (!u.getString("passwordHash").equals(hash)) throw new Exception("Senha incorreta");
        Storage.saveSession(c, email, remember);
        return email;
    }

    public static JSONObject getCurrentUser(Context c) {
        JSONObject sess = Storage.checkSession(c);
        if (sess == null) return null;
        String userId = sess.optString("userId", null);
        if (userId == null) return null;
        JSONObject users = Storage.getUsers(c);
        return users.optJSONObject(userId);
    }

    public static void saveUserData(Context c, JSONObject userData) throws JSONException {
        JSONObject user = getCurrentUser(c);
        if (user == null) return;
        String email = user.optString("email");
        JSONObject users = Storage.getUsers(c);
        JSONObject u = users.getJSONObject(email);
        u.put("data", userData);
        users.put(email, u);
        Storage.saveUsers(c, users);
    }
}
