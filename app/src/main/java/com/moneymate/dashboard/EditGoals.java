package com.moneymate.dashboard;

import static android.view.View.INVISIBLE;

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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditGoals extends AppCompatActivity {
    private String fetchAccountsURL = "http://10.0.2.2/moneymateBackend/fetchAccounts.php";
    private String fetchGoalDetailsURL = "http://10.0.2.2/moneymateBackend/fetchGoalDetails.php";
    private String updateGoalURL = "http://10.0.2.2/moneymateBackend/updateGoal.php";
    private ImageView accountLogo;
    private Button backBtn, updateGoalBtn;
    private EditText targetDateText;
    private TextInputEditText goalNameInput, amountInput;
    private AutoCompleteTextView autoCompleteTextViewAccount;
    private ArrayList<String> accountNames = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_goals);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch userID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");
        String goalID = getIntent().getStringExtra("goalID");

        backBtn = findViewById(R.id.backBtn);
        updateGoalBtn = findViewById(R.id.updateGoalBtn);
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
        updateGoalBtn.setOnClickListener(v -> {
            updateGoal(goalID, userID);
            updateGoalBtn.setEnabled(false);
            updateGoalBtn.postDelayed(() -> updateGoalBtn.setEnabled(true), 1500);
        });

        // Fetch goal data
        fetchGoalDetails(goalID, userID);
    }
    private void fetchAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditGoals.this);

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

                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(EditGoals.this, R.layout.dropdown_item, accountNames);
                        autoCompleteTextViewAccount.setAdapter(accountAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditGoals.this, "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(EditGoals.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
    private void fetchGoalDetails(String goal_id, String user_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditGoals.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchGoalDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject goalObject = jsonResponse.getJSONObject("goalDetails");
                            String goalTitle = goalObject.getString("goal_title");
                            String accountName = goalObject.getString("account_name");
                            String accountType = goalObject.getString("account_type");
                            String amount = goalObject.getString("amount");
                            String targetDate = goalObject.getString("target_date");

                            goalNameInput.setText(goalTitle);
                            autoCompleteTextViewAccount.setText(accountType + " | " + accountName, false);
                            amountInput.setText(amount);
                            targetDateText.setText(targetDate);

                            // Set account logo after fetching
                            updateAccountLogo(accountType);

                        } else {
                            Toast.makeText(EditGoals.this, "Failed to fetch goal details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditGoals.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(EditGoals.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("goalID", goal_id);
                params.put("userID", user_id);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
    private void updateGoal(String goalID, String userID) {

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
        StringRequest request = new StringRequest(Request.Method.POST, updateGoalURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Goal Updated Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditGoals.this, ViewGoal.class);
                            intent.putExtra("goalID", goalID);
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
                params.put("goalID", goalID);
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