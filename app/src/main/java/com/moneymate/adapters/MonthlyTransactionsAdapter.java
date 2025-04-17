package com.moneymate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.moneymate.R;
import com.moneymate.models.MonthlyTransactionsModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyTransactionsAdapter extends RecyclerView.Adapter<MonthlyTransactionsAdapter.MonthlyTransactionsViewHolder> {

    private List<MonthlyTransactionsModel> monthlyTransactionsList;
    private Context context;

    public MonthlyTransactionsAdapter(Context context, List<MonthlyTransactionsModel> monthlyTransactionsList) {
        this.context = context;
        this.monthlyTransactionsList = monthlyTransactionsList;
    }

    @NonNull
    @Override
    public MonthlyTransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.monthly_transaction_item, parent, false);
        return new MonthlyTransactionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyTransactionsViewHolder holder, int position) {
        MonthlyTransactionsModel monthlyTransaction = monthlyTransactionsList.get(position);

        // Format amount with sign
        String sign = monthlyTransaction.getTransactionType().equalsIgnoreCase("Expense") ? "- " : "+ ";
        holder.amountText.setText(sign + "₱" + monthlyTransaction.getTotalAmount());

        // Set color based on type
        if (monthlyTransaction.getTransactionType().equalsIgnoreCase("Expense")) {
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.green));
        }

        // Format and set month text
        String originalDate = monthlyTransaction.getMonthTitle();
        String formattedDate = formatDate(originalDate);
        holder.monthText.setText(formattedDate);

        // Set type and logo
        holder.transactionTypeText.setText(capitalizeFirstLetter(monthlyTransaction.getTransactionType()));
        holder.transactionLogo.setImageResource(getTransactionTypeLogo(monthlyTransaction.getTransactionType()));
    }
    @Override
    public int getItemCount() {
        return monthlyTransactionsList.size();
    }
    public static class MonthlyTransactionsViewHolder extends RecyclerView.ViewHolder {
        ImageView transactionLogo;
        TextView monthText, transactionTypeText, amountText;

        public MonthlyTransactionsViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionLogo = itemView.findViewById(R.id.transactionLogo);
            monthText = itemView.findViewById(R.id.monthText);
            transactionTypeText = itemView.findViewById(R.id.transactionTypeText);
            amountText = itemView.findViewById(R.id.amountText);
        }
    }
    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return inputDate;
        }
    }
    private int getTransactionTypeLogo(String transaction) {
        Map<String, Integer> logos = new HashMap<>();
        logos.put("expense", R.drawable.add_expense);
        logos.put("income", R.drawable.add_income);
        return logos.getOrDefault(transaction.toLowerCase(), R.drawable.logo);
    }
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

}
