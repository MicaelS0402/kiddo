package com.kiddo.controllers;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.kiddo.utils.AppState;
import com.kiddo.utils.DateUtils;
import com.kiddo.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestController {
    private final Context c;
    private final AppState s;

    public QuestController(Context c, AppState s) {
        this.c = c;
        this.s = s;
    }

    public void loadFromUser(JSONObject user) throws JSONException {
        JSONObject data = user.getJSONObject("data");
        s.progress = data.optJSONObject("progress");
        if (s.progress == null) s.progress = new JSONObject();
        s.totalXP = data.optInt("totalXP", 0);
        s.streakCount = data.optInt("streakCount", 0);
        s.lastCompletedDate = data.optString("lastCompletedDate", null);
        s.quests = data.optJSONObject("customQuests");
        if (s.quests == null) s.quests = AppState.defaultQuests();
        s.userName = user.optString("name", "");
        s.userEmail = user.optString("email", "");
        s.avatar = user.optString("avatar", null);
    }

    public JSONObject saveDataObject() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("progress", s.progress);
        o.put("totalXP", s.totalXP);
        o.put("streakCount", s.streakCount);
        o.put("lastCompletedDate", s.lastCompletedDate == null ? JSONObject.NULL : s.lastCompletedDate);
        o.put("customQuests", s.quests);
        return o;
    }

    public void persist() throws JSONException {
        AuthController.saveUserData(c, saveDataObject());
    }

    public void toggleMode() throws JSONException {
        String dateKey = s.getDateKey();
        if (!s.progress.has(dateKey)) s.progress.put(dateKey, new JSONObject());
        JSONObject day = s.progress.getJSONObject(dateKey);
        String current = s.getActiveMode();
        String next = current.equals("week") ? "weekend" : "week";
        day.put("modeOverride", next);
        persist();
    }

    public void resetToAuto() throws JSONException {
        String dateKey = s.getDateKey();
        if (s.progress.has(dateKey)) {
            JSONObject day = s.progress.getJSONObject(dateKey);
            day.remove("modeOverride");
            persist();
        }
    }

    public void changeDate(int dir) {
        s.currentDateIndex += dir;
    }

    public void toggleQuest(String blockId, String questId) throws JSONException {
        String dateKey = s.getDateKey();
        String mode = s.getActiveMode();
        if (!s.progress.has(dateKey)) s.progress.put(dateKey, new JSONObject());
        JSONObject day = s.progress.getJSONObject(dateKey);
        if (!day.has(mode)) day.put(mode, new JSONObject());
        JSONObject modeObj = day.getJSONObject(mode);
        if (!modeObj.has(blockId)) modeObj.put(blockId, new JSONObject());
        JSONObject block = modeObj.getJSONObject(blockId);
        boolean current = block.optBoolean(questId, false);
        block.put(questId, !current);
        JSONArray qList = s.quests.getJSONObject(mode).getJSONArray(blockId);
        int xp = 0;
        for (int i = 0; i < qList.length(); i++) {
            JSONObject q = qList.getJSONObject(i);
            if (q.getString("id").equals(questId)) {
                xp = q.getInt("xp");
                break;
            }
        }
        if (!current) {
            s.totalXP += xp;
            Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            s.totalXP -= xp;
        }
        checkDailyCompletion(dateKey);
        persist();
    }

    private void checkDailyCompletion(String dateKey) throws JSONException {
        String mode = s.getActiveMode();
        boolean allComplete = true;
        for (String blockId : s.blockOrder) {
            JSONArray arr = s.quests.getJSONObject(mode).optJSONArray(blockId);
            if (arr == null) continue;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject q = arr.getJSONObject(i);
                String id = q.getString("id");
                boolean done = s.progress.optJSONObject(dateKey)
                        .optJSONObject(mode)
                        .optJSONObject(blockId)
                        .optBoolean(id, false);
                if (!done) { allComplete = false; break; }
            }
            if (!allComplete) break;
        }
        if (allComplete) updateStreak(dateKey);
    }

    private void updateStreak(String dateKey) throws JSONException {
        String yesterday = previousDay(dateKey);
        if (yesterday != null && yesterday.equals(s.lastCompletedDate)) {
            s.streakCount++;
            if (s.streakCount == 7) {
                s.totalXP += 100;
                s.streakCount = 0;
            }
        } else if (s.lastCompletedDate == null || !s.lastCompletedDate.equals(dateKey)) {
            s.streakCount = 1;
        }
        s.lastCompletedDate = dateKey;
        persist();
    }

    private String previousDay(String dateKey) {
        try {
            String[] p = dateKey.split("-");
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]) - 1;
            int d = Integer.parseInt(p[2]);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.set(y, m, d);
            c.add(java.util.Calendar.DATE, -1);
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        } catch (Exception e) {
            return null;
        }
    }
}
