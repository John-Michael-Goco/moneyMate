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
import com.moneymate.models.InvestmentModel;
import java.util.List;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.InvestmentViewHolder> {

    private List<InvestmentModel> investmentList;
    private Context context;

    public InvestmentAdapter(Context context, List<InvestmentModel> investmentList) {
        this.context = context;
        this.investmentList = investmentList;
    }

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.investment_item, parent, false);
        return new InvestmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        InvestmentModel account = investmentList.get(position);
        holder.investmentAccountLogo.setImageResource(account.getAccountLogo());
        holder.investmentAccountTitle.setText(account.getAccountName());
        holder.investmentAmountText.setText(formatBalance(account.getBalance()));

        // Click listener for the action button (can be modified later)
        holder.investmentActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewAccount.class);
            intent.putExtra("accountID", account.getAccountID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return investmentList.size();
    }

    public static class InvestmentViewHolder extends RecyclerView.ViewHolder {
        ImageView investmentAccountLogo;
        TextView investmentAccountTitle, investmentAmountText;
        ImageButton investmentActionButton;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            investmentAccountLogo = itemView.findViewById(R.id.investmentAccountLogo);
            investmentAccountTitle = itemView.findViewById(R.id.investmentAccountTitle);
            investmentAmountText = itemView.findViewById(R.id.investmentAmountText);
            investmentActionButton = itemView.findViewById(R.id.investmentActionButton);
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
