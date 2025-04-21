package com.moneymate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneymate.R;
import com.moneymate.models.GoalModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GoalLimitedAdapter extends RecyclerView.Adapter<GoalLimitedAdapter.GoalViewHolder> {

    private List<GoalModel> goalsList;
    private Context context;

    public GoalLimitedAdapter(Context context, List<GoalModel> goalsList) {
        this.context = context;
        this.goalsList = goalsList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalModel goal = goalsList.get(position);

        holder.goalTitle.setText(goal.getGoalName());

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy");

            Date goalDate = inputFormat.parse(goal.getGoalDate());
            String formattedDate = outputFormat.format(goalDate);

            if ("Ongoing".equals(goal.getGoalCompletion())) {
                Date today = new Date();
                if (goalDate.before(today)) {
                    holder.goalDate.setTextColor(context.getResources().getColor(R.color.red, null));
                } else {
                    holder.goalDate.setTextColor(context.getResources().getColor(R.color.cerulean, null));
                }
                holder.goalDate.setText(formattedDate);
            } else {
                holder.goalDate.setTextColor(context.getResources().getColor(R.color.green, null));
                holder.goalDate.setText(goal.getGoalCompletion());
            }
        } catch (Exception e) {
            holder.goalDate.setText(goal.getGoalDate());
            e.printStackTrace();
        }

        double accountAmount = goal.getAccountAmount();
        double goalAmount = goal.getGoalAmount();
        int progress = goalAmount == 0 ? 0 : (int) ((accountAmount / goalAmount) * 100);

        if ("Complete".equalsIgnoreCase(goal.getGoalCompletion())) {
            progress = 100;
        }

        if (progress > 100) progress = 100;

        holder.progressBar.setProgress(progress);
        holder.progressText.setText("₱" + accountAmount + " of ₱" + goalAmount);

        if ("Complete".equalsIgnoreCase(goal.getGoalCompletion())) {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.green, null));
            holder.progressText.setText("₱" + goalAmount + " of ₱" + goalAmount);
        } else if (accountAmount >= goalAmount) {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.green, null));
        } else {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.cerulean, null));
        }

        // Hide button since it's limited view
        holder.goalBtn.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTitle, goalDate, progressText;
        ProgressBar progressBar;
        ImageButton goalBtn;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalTitle = itemView.findViewById(R.id.goalTitle);
            goalDate = itemView.findViewById(R.id.goalDate);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            goalBtn = itemView.findViewById(R.id.goalBtn);
        }
    }
}
