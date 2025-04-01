package com.moneymate.adapters;

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
import com.moneymate.models.CashModel;
import java.util.List;

public class CashAdapter extends RecyclerView.Adapter<CashAdapter.CashViewHolder> {

    private List<CashModel> cashList;
    private Context context;

    public CashAdapter(Context context, List<CashModel> cashList) {
        this.context = context;
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
        holder.cashAccountTitle.setText(account.getAccountName());
        holder.cashAmountText.setText(formatBalance(account.getBalance()));

        // Click listener for the action button (can be modified later)
        holder.cashActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewAccount.class);
            intent.putExtra("accountID", account.getAccountID());
            context.startActivity(intent);
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
