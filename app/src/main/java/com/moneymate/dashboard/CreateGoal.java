package com.moneymate.dashboard;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.moneymate.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateGoal extends AppCompatActivity {
    private String fetchAccountsURL = "http://10.0.2.2/moneymateBackend/fetchAccounts.php";
    private String createGoalURL = "http://10.0.2.2/moneymateBackend/createGoal.php";
    private ImageView accountLogo;
    private Button backBtn, createGoalBtn;
    private EditText targetDateText;
    private TextInputEditText goalNameInput, amountInput;
    private AutoCompleteTextView autoCompleteTextViewAccount;
    private ArrayList<String> accountNames = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_goal);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch userID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        backBtn = findViewById(R.id.backBtn);
        createGoalBtn = findViewById(R.id.createGoalBtn);
        accountLogo = findViewById(R.id.accountLogo);
        targetDateText = findViewById(R.id.targetDateText);
        goalNameInput = findViewById(R.id.goalNameInput);
        amountInput = findViewById(R.id.amountInput);
        autoCompleteTextViewAccount = findViewById(R.id.autoCompleteAccountText);

        // Fetch accounts
        fetchAccounts(userID);

        // Handle account selection
        autoCompleteTextViewAccount.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAccount = parent.getItemAtPosition(position).toString();

            // Extract the account type before the " | " symbol
            String accountType = selectedAccount.split(" \\| ")[0];
            updateAccountLogo(accountType);
        });

        // For date picker
        targetDateText.setOnClickListener(v -> showDatePicker());

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());

        // Create Goal
        createGoalBtn.setOnClickListener(v -> {
            createGoal(userID);
            createGoalBtn.setEnabled(false);
            createGoalBtn.postDelayed(() -> createGoalBtn.setEnabled(true), 1500);
        });
    }
    private void fetchAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(CreateGoal.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");

                        accountNames.clear();

                        if (accountsArray.length() == 0) {
                            // No accounts found
                            accountNames.add("No accounts yet");
                            autoCompleteTextViewAccount.setEnabled(false);
                        } else {
                            // Accounts available
                            for (int i = 0; i < accountsArray.length(); i++) {
                                JSONObject accountObject = accountsArray.getJSONObject(i);
                                String accountName = accountObject.getString("account_name");
                                String accountType = accountObject.getString("account_type");
                                accountNames.add(accountType + " | " + accountName);
                            }
                            autoCompleteTextViewAccount.setEnabled(true);
                        }

                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(CreateGoal.this, R.layout.dropdown_item, accountNames);
                        autoCompleteTextViewAccount.setAdapter(accountAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CreateGoal.this, "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(CreateGoal.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
    private void updateAccountLogo(String accountName) {
        Map<String, Integer> accountLogos = new HashMap<>();
        accountLogos.put("Bank Account", R.drawable.bank_logo);
        accountLogos.put("Cash", R.drawable.cash_logo);
        accountLogos.put("Wallet", R.drawable.wallet_logo);
        accountLogos.put("Savings", R.drawable.savings_logo);
        accountLogos.put("Retirement", R.drawable.retirement_logo);
        accountLogos.put("Investment", R.drawable.investment_logo);
        accountLogos.put("Crypto", R.drawable.bitcoin_logo);

        // Update the account logo
        if (accountLogos.containsKey(accountName)) {
            accountLogo.setImageResource(accountLogos.get(accountName));
        } else {
            accountLogo.setImageResource(R.drawable.logo);
        }
    }
    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            targetDateText.setText(dateFormat.format(calendar.getTime()));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void createGoal(String userID) {

        // Get input values
        String goalNameVal = goalNameInput.getText().toString().trim();
        String amountVal = amountInput.getText().toString().trim();
        String accountVal = autoCompleteTextViewAccount.getText().toString().trim();
        String targetDateVal = targetDateText.getText().toString().trim();

        // Extract account_type and account_name from selected accountVal (e.g., "Cash | My Wallet")
        String[] accountParts = accountVal.split(" \\| ");
        String accountTypeVal = accountParts.length > 0 ? accountParts[0].trim() : "";
        String accountNameVal = accountParts.length > 1 ? accountParts[1].trim() : "";

        // Validate input fields
        if (goalNameVal.isEmpty() || amountVal.isEmpty() || accountVal.isEmpty() || targetDateVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Amount
        try {
            double amount = Double.parseDouble(amountVal);
            if (amount <= 0) {
                Toast.makeText(this, "Enter a valid positive amount.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to create the transaction
        StringRequest request = new StringRequest(Request.Method.POST, createGoalURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Goal Created Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateGoal.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Goals");
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GoalError", "JSON Parsing Error: " + e.getMessage());
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
                params.put("goal_name", goalNameVal);
                params.put("amount", amountVal);
                params.put("target_date", targetDateVal);
                params.put("account_type", accountTypeVal);
                params.put("account_name", accountNameVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}