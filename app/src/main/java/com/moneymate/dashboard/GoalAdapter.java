package com.moneymate.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneymate.R;
import com.moneymate.dashboard.Goal;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goalList;
    private OnGoalClickListener listener;

    public GoalAdapter(List<Goal> goalList, OnGoalClickListener listener) {
        this.goalList = goalList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.goalTitle.setText(goal.getTitle());

        holder.goButton.setOnClickListener(v -> listener.onGoalClick(goal));
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTitle;
        ImageView goButton;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalTitle = itemView.findViewById(R.id.goalTitle);
            goButton = itemView.findViewById(R.id.goalLogo);
        }
    }

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }
}