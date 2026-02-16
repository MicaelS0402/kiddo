package com.kiddo.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kiddo.app.R;
import com.kiddo.app.utils.AppState;
import com.kiddo.app.utils.StorageManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextView userName, dailyXP, totalXP, streak, currentDate, modeBadge, progressText;
    private ProgressBar progressBar;
    private Button btnPrev, btnNext, btnToggleMode, btnLogout;
    private RecyclerView listPrep, listResp, listSpace, listDev, listClose;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        userName = v.findViewById(R.id.userName);
        dailyXP = v.findViewById(R.id.dailyXP);
        totalXP = v.findViewById(R.id.totalXP);
        streak = v.findViewById(R.id.streak);
        currentDate = v.findViewById(R.id.currentDate);
        modeBadge = v.findViewById(R.id.modeBadge);
        progressBar = v.findViewById(R.id.progressBar);
        progressText = v.findViewById(R.id.progressText);
        btnPrev = v.findViewById(R.id.btnPrev);
        btnNext = v.findViewById(R.id.btnNext);
        btnToggleMode = v.findViewById(R.id.btnToggleMode);
        btnLogout = v.findViewById(R.id.btnLogout);
        listPrep = v.findViewById(R.id.listPrep);
        listResp = v.findViewById(R.id.listResp);
        listSpace = v.findViewById(R.id.listSpace);
        listDev = v.findViewById(R.id.listDev);
        listClose = v.findViewById(R.id.listClose);

        btnPrev.setOnClickListener(view -> {
            AppState.get().changeDate(-1);
            render();
        });
        btnNext.setOnClickListener(view -> {
            AppState.get().changeDate(1);
            render();
        });
        btnToggleMode.setOnClickListener(view -> {
            AppState.get().toggleMode();
            render();
        });
        btnLogout.setOnClickListener(view -> {
            AppState.get().logout();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AuthFragment())
                    .commit();
        });

        setupList(listPrep);
        setupList(listResp);
        setupList(listSpace);
        setupList(listDev);
        setupList(listClose);

        render();
    }

    private void setupList(RecyclerView rv) {
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void render() {
        if (AppState.get().currentUser != null) {
            userName.setText(AppState.get().currentUser.name);
        }
        String key = AppState.get().getDateString();
        Calendar cal = Calendar.getInstance();
        String[] p = key.split("-");
        cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMM", new Locale("pt","BR"));
        currentDate.setText(sdf.format(cal.getTime()));
        String mode = AppState.get().getActiveMode(key);
        modeBadge.setText(mode.equals("weekend") ? "FIM DE SEMANA" : "SEMANA");
        int dxp = AppState.get().dailyXP(key);
        int mx = AppState.get().maxDaily(key);
        dailyXP.setText(String.valueOf(dxp));
        totalXP.setText(String.valueOf(AppState.get().totalXP));
        streak.setText(AppState.get().streakCount + " 🔥");
        progressBar.setMax(mx == 0 ? 1 : mx);
        progressBar.setProgress(dxp);
        progressText.setText(dxp + " / " + mx + " XP");
        Runnable onChange = this::render;
        listPrep.setAdapter(new QuestAdapter(AppState.get().quests.get(mode).get("prep"), "prep", onChange));
        listResp.setAdapter(new QuestAdapter(AppState.get().quests.get(mode).get("resp"), "resp", onChange));
        listSpace.setAdapter(new QuestAdapter(AppState.get().quests.get(mode).get("space"), "space", onChange));
        listDev.setAdapter(new QuestAdapter(AppState.get().quests.get(mode).get("dev"), "dev", onChange));
        listClose.setAdapter(new QuestAdapter(AppState.get().quests.get(mode).get("close"), "close", onChange));
        btnNext.setEnabled(AppState.get().currentDateIndex < 0);
    }
}
