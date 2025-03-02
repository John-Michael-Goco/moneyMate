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
import com.moneymate.models.CreditModel;
import java.util.List;

public class CreditAdapter extends RecyclerView.Adapter<CreditAdapter.CreditViewHolder> {

    private List<CreditModel> creditList;

    public CreditAdapter(List<CreditModel> creditList) {
        this.creditList = creditList;
    }

    @NonNull
    @Override
    public CreditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.credit_item, parent, false);
        return new CreditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditViewHolder holder, int position) {
        CreditModel account = creditList.get(position);
        holder.creditAccountLogo.setImageResource(account.getLogoResId());
        holder.creditAccountTitle.setText(account.getTitle());
        holder.creditAmountText.setText(account.getAmount());

        // Click listener for the action button (can be modified later)
        holder.creditActionButton.setOnClickListener(v -> {
            // Handle button click
        });
    }

    @Override
    public int getItemCount() {
        return creditList.size();
    }

    public static class CreditViewHolder extends RecyclerView.ViewHolder {
        ImageView creditAccountLogo;
        TextView creditAccountTitle, creditAmountText;
        ImageButton creditActionButton;

        public CreditViewHolder(@NonNull View itemView) {
            super(itemView);
            creditAccountLogo = itemView.findViewById(R.id.creditAccountLogo);
            creditAccountTitle = itemView.findViewById(R.id.creditAccountTitle);
            creditAmountText = itemView.findViewById(R.id.creditAmountText);
            creditActionButton = itemView.findViewById(R.id.creditActionButton);
        }
    }
}
