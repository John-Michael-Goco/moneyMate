package com.moneymate.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.moneymate.R;

public class SelectAccountType extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_account_type);

        // Find Views
        ImageView backBtn = findViewById(R.id.backBtn);

        CardView bankAccount = findViewById(R.id.bankAccount);
        CardView cash = findViewById(R.id.cash);
        CardView wallet = findViewById(R.id.wallet);
        CardView savings = findViewById(R.id.savings);
        CardView retirement = findViewById(R.id.retirement);
        CardView investment = findViewById(R.id.investment);
        CardView crypto = findViewById(R.id.crypto);

        // Handle back button
        backBtn.setOnClickListener(v -> finish());

        // Set click listeners to send data
        bankAccount.setOnClickListener(v -> openNextPage("Bank Account", R.drawable.bank_logo));
        cash.setOnClickListener(v -> openNextPage("Cash", R.drawable.cash_logo));
        wallet.setOnClickListener(v -> openNextPage("Wallet", R.drawable.wallet_logo));
        savings.setOnClickListener(v -> openNextPage("Savings", R.drawable.savings_logo));
        retirement.setOnClickListener(v -> openNextPage("Retirement", R.drawable.retirement_logo));
        investment.setOnClickListener(v -> openNextPage("Investment", R.drawable.investment_logo));
        crypto.setOnClickListener(v -> openNextPage("Crypto", R.drawable.bitcoin_logo));
    }

    private void openNextPage(String accountType, int accountLogo) {
        Intent intent = new Intent(SelectAccountType.this, CreateAccount.class);
        intent.putExtra("accountType", accountType); // Send account type
        intent.putExtra("accountLogo", accountLogo); // Send logo
        startActivity(intent);
    }
}
