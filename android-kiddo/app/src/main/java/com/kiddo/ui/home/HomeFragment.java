package com.kiddo.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.kiddo.R;
import com.kiddo.controllers.AuthController;
import com.kiddo.controllers.QuestController;
import com.kiddo.models.Reward;
import com.kiddo.utils.AppState;
import com.kiddo.utils.DateUtils;
import com.kiddo.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements QuestAdapter.Callback {
    private AppState s;
    private QuestController controller;
    private TextView userName, dailyXP, totalXP, streak, currentDate, modeBadge;
    private ProgressBar progressBar;
    private Switch toggleMode;
    private MaterialButton btnAuto, btnPrevDay, btnNextDay, btnExport, btnReset, btnLogout;
    private LinearLayout blocksContainer;
    private RecyclerView rewardsGrid;
    private RewardsAdapter rewardsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        s = new AppState();
        controller = new QuestController(requireContext(), s);
        JSONObject user = AuthController.getCurrentUser(requireContext());
        if (user == null) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }
        try {
            controller.loadFromUser(user);
        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
        }

        userName = view.findViewById(R.id.userName);
        dailyXP = view.findViewById(R.id.dailyXP);
        totalXP = view.findViewById(R.id.totalXP);
        streak = view.findViewById(R.id.streak);
        currentDate = view.findViewById(R.id.currentDate);
        modeBadge = view.findViewById(R.id.modeBadge);
        progressBar = view.findViewById(R.id.progressBar);
        toggleMode = view.findViewById(R.id.toggleMode);
        btnAuto = view.findViewById(R.id.btnAuto);
        btnPrevDay = view.findViewById(R.id.btnPrevDay);
        btnNextDay = view.findViewById(R.id.btnNextDay);
        btnExport = view.findViewById(R.id.btnExport);
        btnReset = view.findViewById(R.id.btnReset);
        btnLogout = view.findViewById(R.id.btnLogout);
        blocksContainer = view.findViewById(R.id.blocksContainer);
        rewardsGrid = view.findViewById(R.id.rewardsGrid);

        rewardsGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rewardsAdapter = new RewardsAdapter(s.rewards());
        rewardsGrid.setAdapter(rewardsAdapter);

        btnPrevDay.setOnClickListener(v -> { controller.changeDate(-1); render(); });
        btnNextDay.setOnClickListener(v -> { if (s.currentDateIndex < 0) { controller.changeDate(1); render(); } });
        toggleMode.setOnCheckedChangeListener((b, checked) -> {
            try {
                controller.toggleMode();
                render();
            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Erro", Toast.LENGTH_SHORT).show();
            }
        });
        btnAuto.setOnClickListener(v -> {
            try {
                controller.resetToAuto();
                render();
            } catch (JSONException e) {
                Toast.makeText(requireContext(), "Erro", Toast.LENGTH_SHORT).show();
            }
        });
        btnExport.setOnClickListener(v -> exportData());
        btnReset.setOnClickListener(v -> resetProgress());
        btnLogout.setOnClickListener(v -> {
            Storage.clearSession(requireContext());
            requireActivity().recreate();
        });
        render();
    }

    private void render() {
        userName.setText(TextUtils.isEmpty(s.userName) ? "Usuário" : s.userName);
        String dateKey = s.getDateKey();
        currentDate.setText(DateUtils.formatDate(dateKey));
        updateModeUI();
        renderBlocks();
        updateProgress();
        renderRewards();
    }

    private void updateModeUI() {
        String dateKey = s.getDateKey();
        String mode = s.getActiveMode();
        boolean hasOverride = false;
        if (s.progress.has(dateKey)) {
            JSONObject day = s.progress.optJSONObject(dateKey);
            hasOverride = day != null && day.has("modeOverride");
        }
        modeBadge.setText(mode.equals("weekend") ? getString(R.string.fim_semana) : getString(R.string.semana));
        toggleMode.setChecked(mode.equals("weekend"));
        btnAuto.setVisibility(hasOverride ? View.VISIBLE : View.GONE);
    }

    private void renderBlocks() {
        blocksContainer.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(requireContext());
        String dateKey = s.getDateKey();
        String mode = s.getActiveMode();
        for (String blockId : s.blockOrder) {
            JSONArray arr = s.quests.optJSONObject(mode) != null ? s.quests.optJSONObject(mode).optJSONArray(blockId) : null;
            if (arr == null) continue;
            View section = inf.inflate(R.layout.view_block_section, blocksContainer, false);
            TextView sectionName = section.findViewById(R.id.sectionName);
            TextView sectionProgress = section.findViewById(R.id.sectionProgress);
            RecyclerView list = section.findViewById(R.id.questsList);
            MaterialButton btnAdd = section.findViewById(R.id.btnAddQuest);
            sectionName.setText(s.blocks.get(blockId));
            int completed = 0;
            List<QuestItem> items = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject q = arr.optJSONObject(i);
                if (q == null) continue;
                String id = q.optString("id");
                String text = q.optString("text");
                int xp = q.optInt("xp");
                boolean done = s.progress.optJSONObject(dateKey)
                        .optJSONObject(mode)
                        .optJSONObject(blockId)
                        .optBoolean(id, false);
                if (done) completed++;
                items.add(new QuestItem(blockId, id, text, xp, done));
            }
            sectionProgress.setText(completed + "/" + arr.length());
            list.setLayoutManager(new LinearLayoutManager(requireContext()));
            list.setAdapter(new QuestAdapter(items, this));
            btnAdd.setOnClickListener(v -> {
                try {
                    int counter = arr.length() + 100;
                    JSONObject newQuest = new JSONObject();
                    newQuest.put("id", "custom_" + counter);
                    newQuest.put("text", "Novo hábito");
                    newQuest.put("xp", 10);
                    arr.put(newQuest);
                    controller.persist();
                    render();
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Erro", Toast.LENGTH_SHORT).show();
                }
            });
            blocksContainer.addView(section);
        }
    }

    private void updateProgress() {
        String dateKey = s.getDateKey();
        String mode = s.getActiveMode();
        int daily = 0;
        int max = 0;
        JSONObject day = s.progress.optJSONObject(dateKey);
        for (String blockId : s.blockOrder) {
            JSONArray arr = s.quests.optJSONObject(mode) != null ? s.quests.optJSONObject(mode).optJSONArray(blockId) : null;
            if (arr == null) continue;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject q = arr.optJSONObject(i);
                if (q == null) continue;
                max += q.optInt("xp", 0);
                boolean done = day != null
                        && day.optJSONObject(mode) != null
                        && day.optJSONObject(mode).optJSONObject(blockId) != null
                        && day.optJSONObject(mode).optJSONObject(blockId).optBoolean(q.optString("id"), false);
                if (done) daily += q.optInt("xp", 0);
            }
        }
        dailyXP.setText(String.valueOf(daily));
        totalXP.setText(String.valueOf(s.totalXP));
        streak.setText(s.streakCount + " 🔥");
        int percent = max > 0 ? Math.min((int) Math.round((daily * 100.0) / max), 100) : 0;
        progressBar.setProgress(percent);
    }

    private void renderRewards() {
        rewardsAdapter.setTotalXP(s.totalXP);
    }

    @Override
    public void onToggle(String blockId, String questId) {
        try {
            controller.toggleQuest(blockId, questId);
            render();
        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Erro", Toast.LENGTH_SHORT).show();
        }
    }

    public void exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put("userName", s.userName);
            data.put("userEmail", s.userEmail);
            data.put("progress", s.progress);
            data.put("totalXP", s.totalXP);
            data.put("streakCount", s.streakCount);
            data.put("lastCompletedDate", s.lastCompletedDate == null ? JSONObject.NULL : s.lastCompletedDate);
            data.put("customQuests", s.quests);
            data.put("version", "3.0.0");
        } catch (JSONException ignored) {}
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_TEXT, data.toString());
        startActivity(Intent.createChooser(share, "Exportar Dados"));
        Toast.makeText(requireContext(), "Dados exportados", Toast.LENGTH_SHORT).show();
    }

    public void resetProgress() {
        s.progress = new JSONObject();
        s.totalXP = 0;
        s.streakCount = 0;
        s.lastCompletedDate = null;
        s.currentDateIndex = 0;
        s.ensureQuests();
        try {
            controller.persist();
        } catch (JSONException ignored) {}
        render();
        Toast.makeText(requireContext(), "Reset completo", Toast.LENGTH_SHORT).show();
    }

    public void openAccount() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new com.kiddo.ui.account.AccountFragment())
                .addToBackStack(null)
                .commit();
    }

    public void openProgress() {
        Toast.makeText(requireContext(), "Progresso", Toast.LENGTH_SHORT).show();
    }
}
