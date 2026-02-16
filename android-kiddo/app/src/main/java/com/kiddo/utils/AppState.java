package com.kiddo.utils;

import android.content.Context;

import com.kiddo.models.Quest;
import com.kiddo.models.Reward;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {
    public JSONObject progress = new JSONObject();
    public JSONObject quests;
    public int totalXP = 0;
    public int streakCount = 0;
    public String lastCompletedDate = null;
    public int currentDateIndex = 0;
    public String userName = "";
    public String userEmail = "";
    public String avatar = null;

    public final Map<String, String> blocks = new HashMap<String, String>() {{
        put("prep", "Preparação do Dia");
        put("resp", "Responsabilidades");
        put("space", "Cuidado com o Espaço");
        put("dev", "Desenvolvimento Pessoal");
        put("close", "Encerramento do Dia");
    }};

    public final String[] blockOrder = new String[]{"prep","resp","space","dev","close"};

    public List<Reward> rewards() {
        List<Reward> list = new ArrayList<>();
        list.add(new Reward("bronze","🥉","Conquista Inicial",1000));
        list.add(new Reward("silver","🥈","Conquista Especial",2000));
        list.add(new Reward("gold","🥇","Grande Conquista",4000));
        list.add(new Reward("trophy","🏆","Conquista Lendária",8000));
        return list;
    }

    public String getDateKey() {
        return DateUtils.getDateString(currentDateIndex);
    }

    public String getActiveMode() {
        String dateKey = getDateKey();
        try {
            if (progress.has(dateKey)) {
                JSONObject o = progress.getJSONObject(dateKey);
                if (o.has("modeOverride")) return o.getString("modeOverride");
            }
        } catch (JSONException ignored) {}
        return DateUtils.isWeekend(dateKey) ? "weekend" : "week";
    }

    public void resetToAuto() {
        String dateKey = getDateKey();
        if (progress.has(dateKey)) {
            progress.remove(dateKey);
        }
    }

    public static JSONObject defaultQuests() {
        JSONObject root = new JSONObject();
        try {
            JSONObject week = new JSONObject();
            JSONObject weekend = new JSONObject();
            week.put("prep", array(
                    new Quest("breakfast","Tomar café da manhã",5),
                    new Quest("brush1","Escovar os dentes",10),
                    new Quest("dress","Vestir a roupa da escola",5),
                    new Quest("hair","Pentear o cabelo",5)
            ));
            week.put("resp", array(
                    new Quest("backpack","Arrumar a mochila",10),
                    new Quest("check","Checar se trouxe tudo da escola",10),
                    new Quest("homework","Fazer lição de casa",15)
            ));
            week.put("space", array(
                    new Quest("dishes","Colocar o prato na pia",5),
                    new Quest("shoes","Guardar sapatos na sapateira",5),
                    new Quest("laundry","Guardar roupa suja no cesto",5),
                    new Quest("towel","Guardar a toalha no lugar",5)
            ));
            week.put("dev", array(
                    new Quest("read","Ler um livro",15),
                    new Quest("diary","Escrever o diário",15)
            ));
            week.put("close", array(
                    new Quest("shower","Tomar banho",10),
                    new Quest("brush2","Escovar os dentes",10),
                    new Quest("sleep","Ir dormir no horário",10)
            ));
            weekend.put("prep", array(
                    new Quest("wk_breakfast","Tomar café da manhã",5),
                    new Quest("wk_brush","Escovar os dentes",10)
            ));
            weekend.put("resp", array(
                    new Quest("wk_help","Ajudar em casa",15),
                    new Quest("wk_organize","Organizar brinquedos",10)
            ));
            weekend.put("space", array(
                    new Quest("wk_room","Arrumar o quarto",15),
                    new Quest("wk_dishes","Ajudar a lavar louça",10)
            ));
            weekend.put("dev", array(
                    new Quest("wk_read","Ler um livro",15),
                    new Quest("wk_hobby","Praticar hobby",15),
                    new Quest("wk_family","Tempo em família",20)
            ));
            weekend.put("close", array(
                    new Quest("wk_shower","Tomar banho",10),
                    new Quest("wk_brush2","Escovar os dentes",10),
                    new Quest("wk_sleep","Ir dormir no horário",10)
            ));
            root.put("week", week);
            root.put("weekend", weekend);
        } catch (JSONException ignored) {}
        return root;
    }

    private static JSONArray array(Quest... qs) throws JSONException {
        JSONArray a = new JSONArray();
        for (Quest q : qs) {
            JSONObject o = new JSONObject();
            o.put("id", q.id);
            o.put("text", q.text);
            o.put("xp", q.xp);
            a.put(o);
        }
        return a;
    }

    public void ensureQuests() {
        if (quests == null) quests = defaultQuests();
    }
}
