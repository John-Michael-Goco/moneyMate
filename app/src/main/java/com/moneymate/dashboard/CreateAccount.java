package com.moneymate.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.moneymate.R;
import com.moneymate.auth.Login;
import com.moneymate.auth.Register;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    private static final String URL = "http://10.0.2.2/moneymateBackend/createAccount.php";

    private TextInputEditText accountTypeInput, accountNameInput, accountNumberInput, currencyInput, balanceInput, logoInput;
    private Button createAccountBtn, backBtn;
    private ImageView accountLogo;
    private int accLogo;

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

        // Retrieve data from last page
        String accountType = getIntent().getStringExtra("accountType");
        accLogo = getIntent().getIntExtra("accountLogo", R.drawable.logo);

        // Retrieve user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        // Find views
        accountTypeInput = findViewById(R.id.accountTypeText);
        accountNameInput = findViewById(R.id.accountNameText);
        accountNumberInput = findViewById(R.id.accountNumberText);
        currencyInput = findViewById(R.id.currencyText);
        balanceInput = findViewById(R.id.startingBalanceText);
        accountLogo = findViewById(R.id.accountLogoImage);

        // Set data
        accountTypeInput.setText(accountType);
        accountLogo.setImageResource(accLogo);

        // Handle back button
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        // Handle Create Account button
        createAccountBtn = findViewById(R.id.createAccountBtn);
        createAccountBtn.setOnClickListener(v -> {
            createAccount(userID, accLogo);
            createAccountBtn.setEnabled(false);
            createAccountBtn.postDelayed(() -> createAccountBtn.setEnabled(true), 1500);
        });
    }

    private void createAccount(String userID, int accLogo) {

        // Get input values
        String accountTypeVal = accountTypeInput.getText().toString().trim();
        String accountNameVal = accountNameInput.getText().toString().trim();
        String accountNumberVal = accountNumberInput.getText().toString().trim();
        String currencyVal = currencyInput.getText().toString().trim();
        String balanceVal = balanceInput.getText().toString().trim();

        String accountLogoVal = String.valueOf(accLogo);

        // Input validation
        if (accountTypeVal.isEmpty() || accountNameVal.isEmpty() || accountNumberVal.isEmpty() ||
                currencyVal.isEmpty() || balanceVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to update the student
        StringRequest request = new StringRequest(Request.Method.POST, URL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragmentContainer, new AccountFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                            finish();
                        } else if ("exists".equals(status)) {
                            Toast.makeText(this, "Account already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("UpdateError", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Parsing Error. Try again later.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request Error: " + error.toString());
                    Toast.makeText(this, "Request Error. Check your connection.", Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("account_type", accountTypeVal);
                params.put("account_logo", accountLogoVal);
                params.put("account_name", accountNameVal);
                params.put("account_number", accountNumberVal);
                params.put("currency", currencyVal);
                params.put("balance", balanceVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}