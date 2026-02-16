package com.kiddo.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.R;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.VH> {
    public interface Callback {
        void onToggle(String blockId, String questId);
    }
    private final List<QuestItem> data;
    private final Callback cb;
    public QuestAdapter(List<QuestItem> data, Callback cb) {
        this.data = data;
        this.cb = cb;
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        QuestItem it = data.get(i);
        h.text.setText(it.text);
        h.xp.setText("+" + it.xp + " XP");
        h.check.setOnCheckedChangeListener(null);
        h.check.setChecked(it.done);
        h.itemView.setOnClickListener(v -> cb.onToggle(it.blockId, it.id));
        h.check.setOnClickListener(v -> cb.onToggle(it.blockId, it.id));
    }
    @Override
    public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        CheckBox check; TextView text; TextView xp;
        VH(@NonNull View v) {
            super(v);
            check = v.findViewById(R.id.check);
            text = v.findViewById(R.id.text);
            xp = v.findViewById(R.id.xp);
        }
    }
}
