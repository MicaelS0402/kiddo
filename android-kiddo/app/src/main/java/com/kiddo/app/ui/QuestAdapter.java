package com.kiddo.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kiddo.app.R;
import com.kiddo.app.models.Quest;
import com.kiddo.app.utils.AppState;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.VH> {
    private final List<Quest> items;
    private final String blockId;
    private final Runnable onChange;
    public QuestAdapter(List<Quest> items, String blockId) {
        this(items, blockId, null);
    }
    public QuestAdapter(List<Quest> items, String blockId, Runnable onChange) {
        this.items = items;
        this.blockId = blockId;
        this.onChange = onChange;
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        Quest q = items.get(p);
        h.text.setText(q.text);
        h.xp.setText("+" + q.xp + " XP");
        String key = AppState.get().getDateString();
        String mode = AppState.get().getActiveMode(key);
        boolean done = AppState.get().progress.get(key) != null
                && AppState.get().progress.get(key).states.getOrDefault(mode, new java.util.HashMap<>())
                .getOrDefault(blockId, new java.util.HashMap<>())
                .getOrDefault(q.id, false);
        h.check.setChecked(done);
        View.OnClickListener toggle = v -> {
            AppState.get().toggleQuest(blockId, q.id);
            notifyItemChanged(p);
            if (onChange != null) onChange.run();
        };
        h.itemView.setOnClickListener(toggle);
        h.check.setOnClickListener(toggle);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox check;
        TextView text;
        TextView xp;
        VH(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check);
            text = itemView.findViewById(R.id.text);
            xp = itemView.findViewById(R.id.xp);
        }
    }
}
