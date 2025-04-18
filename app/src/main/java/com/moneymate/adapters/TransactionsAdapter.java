package com.moneymate.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneymate.R;
import com.moneymate.dashboard.ViewAccount;
import com.moneymate.dashboard.ViewTransaction;
import com.moneymate.models.TransactionsModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder> {

    private List<TransactionsModel> transactionsList;
    private Context context;

    public TransactionsAdapter(Context context, List<TransactionsModel> transactionsList) {
        this.context = context;
        this.transactionsList = transactionsList;
    }

    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        TransactionsModel transaction = transactionsList.get(position);

        holder.categoryText.setText(transaction.getCategory());

        // Set amount with + or - sign
        String sign = transaction.getCategoryType().equalsIgnoreCase("Expense") ? "- " : "+ ";
        holder.amountText.setText(sign + "₱" + transaction.getAmount());

        // Set color
        if (transaction.getCategoryType().equalsIgnoreCase("Expense")) {
            holder.amountText.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            holder.amountText.setTextColor(context.getResources().getColor(R.color.green));
        }

        // Format date
        String originalDate = transaction.getTransactionDate();
        String formattedDate = formatDate(originalDate);
        holder.titleText.setText(transaction.getTransactionName() + " | " + formattedDate);

        // Set transaction logo based on category
        holder.transactionLogo.setImageResource(getCategoryLogo(transaction.getCategory()));

        // Set click action on the arrow button
        holder.transactionBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewTransaction.class);
            intent.putExtra("transactionID", transaction.getTransactionID());
            intent.putExtra("accountID", transaction.getAccountID());
            context.startActivity(intent);
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public static class TransactionsViewHolder extends RecyclerView.ViewHolder {
        ImageView transactionLogo;
        TextView categoryText, titleText, amountText;
        ImageButton transactionBtn;

        public TransactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionLogo = itemView.findViewById(R.id.transactionLogo);
            categoryText = itemView.findViewById(R.id.categoryText);
            titleText = itemView.findViewById(R.id.titleText);
            amountText = itemView.findViewById(R.id.amountText);
            transactionBtn = itemView.findViewById(R.id.transactionBtn);
        }
    }
    private String formatDate(String inputDate) {
        try {
            // Adjust the input format based on how your date is stored (e.g., "2025-04-12")
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMMM d, yyyy");
            java.util.Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return inputDate; // fallback if something goes wrong
        }
    }
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
        logos.put("Salary & Paycheck", R.drawable.salary_ic);
        logos.put("Business & Profession", R.drawable.business_ic);
        logos.put("Investments", R.drawable.investment_ic);
        logos.put("Savings", R.drawable.salary_ic);
        logos.put("Retirement", R.drawable.retirement_ic);
        logos.put("Others", R.drawable.others_ic);
        logos.put("Transfer", R.drawable.transfer);

        return logos.getOrDefault(category, R.drawable.logo);
    }
}