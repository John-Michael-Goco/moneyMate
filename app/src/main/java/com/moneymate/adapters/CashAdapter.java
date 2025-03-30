package com.moneymate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.moneymate.R;
import com.moneymate.models.CashModel;
import java.util.List;

public class CashAdapter extends RecyclerView.Adapter<CashAdapter.CashViewHolder> {

    private List<CashModel> cashList;

    public CashAdapter(List<CashModel> cashList) {
        this.cashList = cashList;
    }

    @NonNull
    @Override
    public CashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cash_item, parent, false);
        return new CashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CashViewHolder holder, int position) {
        CashModel account = cashList.get(position);
        holder.cashAccountLogo.setImageResource(account.getAccountLogo());
        holder.cashAccountTitle.setText(account.getAccountType());
        holder.cashAmountText.setText(formatBalance(account.getBalance()));

        // Click listener for the action button (can be modified later)
        holder.cashActionButton.setOnClickListener(v -> {
            // Handle button click
        });
    }

    @Override
    public int getItemCount() {
        return cashList.size();
    }

    public static class CashViewHolder extends RecyclerView.ViewHolder {
        ImageView cashAccountLogo;
        TextView cashAccountTitle, cashAmountText;
        ImageButton cashActionButton;

        public CashViewHolder(@NonNull View itemView) {
            super(itemView);
            cashAccountLogo = itemView.findViewById(R.id.cashAccountLogo);
            cashAccountTitle = itemView.findViewById(R.id.cashAccountTitle);
            cashAmountText = itemView.findViewById(R.id.cashAmountText);
            cashActionButton = itemView.findViewById(R.id.cashActionButton);
        }
    }

    private String formatBalance(String balance) {
        try {
            double amount = Double.parseDouble(balance);
            return String.format("₱%,.2f", amount);
        } catch (NumberFormatException e) {
            return balance;
        }
    }
}
