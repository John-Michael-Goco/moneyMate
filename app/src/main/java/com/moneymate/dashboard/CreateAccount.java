package com.moneymate.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.moneymate.R;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve data
        String accountType = getIntent().getStringExtra("accountType");
        int accountLogo = getIntent().getIntExtra("accountLogo", R.drawable.logo);

        // Find views
        TextInputEditText textView = findViewById(R.id.accountTypeText);
        ImageView imageView = findViewById(R.id.accountLogoImage);

        // Set data
        textView.setText(accountType);
        imageView.setImageResource(accountLogo);

        // Handle back button
        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());
    }
}