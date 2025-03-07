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
import com.moneymate.models.InvestmentModel;
import java.util.List;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.InvestmentViewHolder> {

    private List<InvestmentModel> investmentList;

    public InvestmentAdapter(List<InvestmentModel> investmentList) {
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
        holder.investmentAccountLogo.setImageResource(account.getLogoResId());
        holder.investmentAccountTitle.setText(account.getTitle());
        holder.investmentAmountText.setText(account.getAmount());

        // Click listener for the action button (can be modified later)
        holder.investmentActionButton.setOnClickListener(v -> {
            // Handle button click
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
}
