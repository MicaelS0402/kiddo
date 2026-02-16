package com.kiddo.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kiddo.R;
import com.kiddo.models.Reward;

import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.VH> {
    private final List<Reward> data;
    private int totalXP = 0;
    public RewardsAdapter(List<Reward> data) { this.data = data; }
    public void setTotalXP(int xp) { this.totalXP = xp; notifyDataSetChanged(); }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Reward r = data.get(i);
        h.icon.setText(r.icon);
        h.name.setText(r.name);
        h.cost.setText(r.cost + " XP");
        h.itemView.setAlpha(totalXP >= r.cost ? 1f : 0.5f);
    }
    @Override
    public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView icon, name, cost;
        VH(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            name = v.findViewById(R.id.name);
            cost = v.findViewById(R.id.cost);
        }
    }
}
