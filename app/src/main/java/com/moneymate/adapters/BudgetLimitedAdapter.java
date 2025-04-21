package com.moneymate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneymate.R;
import com.moneymate.models.BudgetModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetLimitedAdapter extends RecyclerView.Adapter<BudgetLimitedAdapter.BudgetViewHolder> {

    private List<BudgetModel> budgetsList;
    private Context context;

    public BudgetLimitedAdapter(Context context, List<BudgetModel> budgetsList) {
        this.context = context;
        this.budgetsList = budgetsList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_item, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetModel budget = budgetsList.get(position);

        holder.budgetTitle.setText(budget.getBudgetCategory() + " | " + budget.getBudgetName());

        // Set progress bar
        double spent = budget.getTotalSpent();
        double limit = budget.getBudgetAmount();
        int progress = (int) ((spent / limit) * 100);
        if (progress > 100) progress = 100;

        holder.progressBar.setProgress(progress);
        holder.progressText.setText("₱" + spent + " of ₱" + limit);

        // Change color based on budget usage
        if (spent > limit) {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.red, null));
        } else {
            holder.progressBar.setProgressTintList(context.getResources().getColorStateList(R.color.green, null));
        }

        // category logo based on category name
        holder.categoryLogo.setImageResource(getCategoryLogo(budget.getBudgetCategory()));

        // Set click action on the arrow button
        holder.budgetBtn.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return budgetsList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryLogo;
        TextView budgetTitle, progressText;
        ProgressBar progressBar;
        ImageButton budgetBtn;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryLogo = itemView.findViewById(R.id.categoryLogo);
            budgetTitle = itemView.findViewById(R.id.budgetTitle);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
            budgetBtn = itemView.findViewById(R.id.budgetBtn);
        }
    }
    // Sample logo setter – you can replace this with a better approach
    private int getCategoryLogo(String category) {
        Map<String, Integer> logos = new HashMap<>();
        logos.put("Bills & Utilities", R.drawable.bills_ic);
        logos.put("Drink & Dine", R.drawable.drink_ic);
        logos.put("Education", R.drawable.education_ic);
        logos.put("Entertainment", R.drawable.entertainment_ic);
        logos.put("Food & Grocery", R.drawable.food_ic);
        logos.put("Personal Care", R.drawable.personal_care_ic);
        logos.put("Pet Care", R.drawable.pet_care_ic);
        logos.put("Shopping", R.drawable.shopping_ic);
        logos.put("Others", R.drawable.others_ic);

        return logos.getOrDefault(category, R.drawable.logo);
    }
}
