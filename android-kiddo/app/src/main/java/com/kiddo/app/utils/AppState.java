package com.kiddo.app.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kiddo.app.models.Quest;
import com.kiddo.app.models.User;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppState {
    private static AppState INSTANCE;
    private static final Gson gson = new Gson();
    private final Context ctx;
    public User currentUser;
    public int currentDateIndex = 0;
    public int totalXP = 0;
    public int streakCount = 0;
    public String lastCompletedDate = null;
    public Map<String, DayData> progress = new HashMap<>();
    public Map<String, Map<String, List<Quest>>> quests = defaultQuests();

    private AppState(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        if (hasActiveSession()) {
            StorageManager.Session s = StorageManager.getSession();
            Map<String, User> users = StorageManager.getUsers();
            currentUser = users.get(s.userId);
            loadUserData();
        }
    }

    public static void init(Context ctx) {
        if (INSTANCE == null) INSTANCE = new AppState(ctx);
    }

    public static AppState get() {
        return INSTANCE;
    }

    public boolean hasActiveSession() {
        StorageManager.Session s = StorageManager.getSession();
        if (s == null) return false;
        Map<String, User> users = StorageManager.getUsers();
        return users.containsKey(s.userId);
    }

    public void setCurrentUser(User u, boolean remember) {
        currentUser = u;
        StorageManager.saveSession(u.email, remember);
        loadUserData();
    }

    public void logout() {
        saveUserData();
        StorageManager.clearSession();
        currentUser = null;
    }

    public void loadUserData() {
        if (currentUser == null) return;
        String json = StorageManager.getDataForUser(currentUser.email);
        if (json != null) {
            Type t = new TypeToken<SavedData>() {}.getType();
            SavedData d = gson.fromJson(json, t);
            if (d != null) {
                this.progress = d.progress != null ? d.progress : new HashMap<>();
                this.totalXP = d.totalXP;
                this.streakCount = d.streakCount;
                this.lastCompletedDate = d.lastCompletedDate;
                if (d.customQuests != null) this.quests = d.customQuests;
            }
        }
    }

    public void saveUserData() {
        if (currentUser == null) return;
        SavedData d = new SavedData();
        d.progress = progress;
        d.totalXP = totalXP;
        d.streakCount = streakCount;
        d.lastCompletedDate = lastCompletedDate;
        d.customQuests = quests;
        StorageManager.saveDataForUser(currentUser.email, gson.toJson(d));
    }

    public String getDateString() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, currentDateIndex);
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }

    public boolean isWeekend(String dateKey) {
        try {
            String[] p = dateKey.split("-");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]), 0, 0, 0);
            int d = c.get(Calendar.DAY_OF_WEEK);
            return d == Calendar.SATURDAY || d == Calendar.SUNDAY;
        } catch (Exception e) {
            return false;
        }
    }

    public String getActiveMode(String dateKey) {
        DayData d = progress.get(dateKey);
        if (d != null && d.modeOverride != null) return d.modeOverride;
        return isWeekend(dateKey) ? "weekend" : "week";
    }

    public void toggleMode() {
        String key = getDateString();
        DayData d = progress.get(key);
        if (d == null) d = new DayData();
        String mode = getActiveMode(key);
        d.modeOverride = mode.equals("week") ? "weekend" : "week";
        progress.put(key, d);
        saveUserData();
    }

    public void changeDate(int dir) {
        currentDateIndex += dir;
    }

    public void toggleQuest(String blockId, String questId) {
        String key = getDateString();
        String mode = getActiveMode(key);
        DayData d = progress.get(key);
        if (d == null) d = new DayData();
        if (!d.states.containsKey(mode)) d.states.put(mode, new HashMap<String, Map<String, Boolean>>());
        Map<String, Map<String, Boolean>> byBlock = d.states.get(mode);
        if (!byBlock.containsKey(blockId)) byBlock.put(blockId, new HashMap<String, Boolean>());
        Map<String, Boolean> questsState = byBlock.get(blockId);
        boolean cur = questsState.getOrDefault(questId, false);
        questsState.put(questId, !cur);
        Quest q = findQuest(mode, blockId, questId);
        if (q != null) totalXP += !cur ? q.xp : -q.xp;
        progress.put(key, d);
        checkDailyCompletion(key);
        saveUserData();
    }

    public Quest findQuest(String mode, String blockId, String questId) {
        List<Quest> list = quests.get(mode).get(blockId);
        if (list == null) return null;
        for (Quest q : list) if (q.id.equals(questId)) return q;
        return null;
    }

    public int dailyXP(String dateKey) {
        String mode = getActiveMode(dateKey);
        int xp = 0;
        for (String block : BLOCK_ORDER) {
            List<Quest> qs = quests.get(mode).get(block);
            if (qs == null) continue;
            for (Quest q : qs) {
                boolean done = progress.get(dateKey) != null
                        && progress.get(dateKey).states.getOrDefault(mode, new HashMap<>())
                        .getOrDefault(block, new HashMap<>())
                        .getOrDefault(q.id, false);
                if (done) xp += q.xp;
            }
        }
        return xp;
    }

    public int maxDaily(String dateKey) {
        String mode = getActiveMode(dateKey);
        int max = 0;
        for (String block : BLOCK_ORDER) {
            List<Quest> qs = quests.get(mode).get(block);
            if (qs == null) continue;
            for (Quest q : qs) max += q.xp;
        }
        return max;
    }

    private void checkDailyCompletion(String dateKey) {
        String mode = getActiveMode(dateKey);
        boolean hasAny = false;
        for (String b : BLOCK_ORDER) {
            List<Quest> qs = quests.get(mode).get(b);
            if (qs != null && !qs.isEmpty()) { hasAny = true; break; }
        }
        if (!hasAny) return;
        boolean all = true;
        for (String b : BLOCK_ORDER) {
            List<Quest> qs = quests.get(mode).get(b);
            if (qs == null) qs = new ArrayList<>();
            for (Quest q : qs) {
                boolean done = progress.get(dateKey) != null
                        && progress.get(dateKey).states.getOrDefault(mode, new HashMap<>())
                        .getOrDefault(b, new HashMap<>())
                        .getOrDefault(q.id, false);
                if (!done) { all = false; break; }
            }
            if (!all) break;
        }
        if (all) updateStreak(dateKey);
    }

    private void updateStreak(String dateKey) {
        Calendar c = Calendar.getInstance();
        String[] p = dateKey.split("-");
        c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        c.add(Calendar.DATE, -1);
        Date y = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String yKey = sdf.format(y);
        if (yKey.equals(lastCompletedDate)) {
            streakCount++;
            if (streakCount == 7) {
                totalXP += 100;
                streakCount = 0;
            }
        } else if (!dateKey.equals(lastCompletedDate)) {
            streakCount = 1;
        }
        lastCompletedDate = dateKey;
    }

    public static final String[] BLOCK_ORDER = {"prep","resp","space","dev","close"};

    public static Map<String, Map<String, List<Quest>>> defaultQuests() {
        Map<String, Map<String, List<Quest>>> all = new LinkedHashMap<>();
        Map<String, List<Quest>> week = new LinkedHashMap<>();
        Map<String, List<Quest>> weekend = new LinkedHashMap<>();
        week.put("prep", list(
                new Quest("breakfast","Tomar café da manhã",5),
                new Quest("brush1","Escovar os dentes",10),
                new Quest("dress","Vestir a roupa da escola",5),
                new Quest("hair","Pentear o cabelo",5)
        ));
        week.put("resp", list(
                new Quest("backpack","Arrumar a mochila",10),
                new Quest("check","Checar se trouxe tudo da escola",10),
                new Quest("homework","Fazer lição de casa",15)
        ));
        week.put("space", list(
                new Quest("dishes","Colocar o prato na pia",5),
                new Quest("shoes","Guardar sapatos na sapateira",5),
                new Quest("laundry","Guardar roupa suja no cesto",5),
                new Quest("towel","Guardar a toalha no lugar",5)
        ));
        week.put("dev", list(
                new Quest("read","Ler um livro",15),
                new Quest("diary","Escrever o diário",15)
        ));
        week.put("close", list(
                new Quest("shower","Tomar banho",10),
                new Quest("brush2","Escovar os dentes",10),
                new Quest("sleep","Ir dormir no horário",10)
        ));
        weekend.put("prep", list(
                new Quest("wk_breakfast","Tomar café da manhã",5),
                new Quest("wk_brush","Escovar os dentes",10)
        ));
        weekend.put("resp", list(
                new Quest("wk_help","Ajudar em casa",15),
                new Quest("wk_organize","Organizar brinquedos",10)
        ));
        weekend.put("space", list(
                new Quest("wk_room","Arrumar o quarto",15),
                new Quest("wk_dishes","Ajudar a lavar louça",10)
        ));
        weekend.put("dev", list(
                new Quest("wk_read","Ler um livro",15),
                new Quest("wk_hobby","Praticar hobby",15),
                new Quest("wk_family","Tempo em família",20)
        ));
        weekend.put("close", list(
                new Quest("wk_shower","Tomar banho",10),
                new Quest("wk_brush2","Escovar os dentes",10),
                new Quest("wk_sleep","Ir dormir no horário",10)
        ));
        all.put("week", week);
        all.put("weekend", weekend);
        return all;
    }

    private static List<Quest> list(Quest... qs) {
        List<Quest> l = new ArrayList<>();
        for (Quest q : qs) l.add(q);
        return l;
    }

    public static class DayData {
        public String modeOverride;
        public Map<String, Map<String, Map<String, Boolean>>> states = new HashMap<>();
    }

    public static class SavedData {
        public Map<String, DayData> progress;
        public int totalXP;
        public int streakCount;
        public String lastCompletedDate;
        public Map<String, Map<String, List<Quest>>> customQuests;
    }
}
