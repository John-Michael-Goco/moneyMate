package com.moneymate.dashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class Transfer extends AppCompatActivity {

    private String fetchAccountsURL = "http://10.0.2.2/moneymateBackend/fetchAccounts.php";
    private String createTransferURL = "http://10.0.2.2/moneymateBackend/createTransfer.php";
    private ImageView accountFromLogo, accountToLogo;
    private Button backBtn, createTransfer;
    private EditText dateText;
    private TextInputEditText transferNameInput, amountInput, noteInput;
    private AutoCompleteTextView autoCompleteTextFrom, autoCompleteTextTo;
    private ArrayList<String> accountNames = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transfer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accountFromLogo = findViewById(R.id.accountFromLogo);
        accountToLogo = findViewById(R.id.accountToLogo);
        backBtn = findViewById(R.id.backBtn);
        dateText = findViewById(R.id.dateText);
        transferNameInput = findViewById(R.id.transferName);
        amountInput = findViewById(R.id.amountInput);
        noteInput = findViewById(R.id.noteInput);

        // Initialize AutoCompleteTextView
        autoCompleteTextFrom = findViewById(R.id.autoCompleteTextFrom);
        autoCompleteTextTo = findViewById(R.id.autoCompleteTextTo);

        // Set default date (current date)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateText.setText(dateFormat.format(calendar.getTime()));

        // Retrieve userID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        // Fetch accounts after setting up categories
        fetchAccounts(userID);

        // Handle account selection "From"
        autoCompleteTextFrom.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAccount = parent.getItemAtPosition(position).toString();

            // Extract the account type before the " | " symbol
            String accountType = selectedAccount.split(" \\| ")[0];
            updateAccountFromLogo(accountType);
        });

        // Handle account selection "To"
        autoCompleteTextTo.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAccount = parent.getItemAtPosition(position).toString();

            // Extract the account type before the " | " symbol
            String accountType = selectedAccount.split(" \\| ")[0];
            updateAccountToLogo(accountType);
        });

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());
        // For date picker
        dateText.setOnClickListener(v -> showDatePicker());

        // Handle Create Transfer
        createTransfer = findViewById(R.id.createTransfer);
        createTransfer.setOnClickListener(v -> {
            createTransaction(userID);
            createTransfer.setEnabled(false);
            createTransfer.postDelayed(() -> createTransfer.setEnabled(true), 1500);
        });
    }

    private void fetchAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(Transfer.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");

                        accountNames.clear(); // Clear existing account names

                        if (accountsArray.length() == 0) {
                            // No accounts found
                            accountNames.add("No accounts yet");
                            autoCompleteTextTo.setEnabled(false);
                            autoCompleteTextFrom.setEnabled(false);
                        } else {
                            // Accounts available
                            for (int i = 0; i < accountsArray.length(); i++) {
                                JSONObject accountObject = accountsArray.getJSONObject(i);
                                String accountName = accountObject.getString("account_name");
                                String accountType = accountObject.getString("account_type");
                                accountNames.add(accountType + " | " + accountName);
                            }
                            autoCompleteTextTo.setEnabled(true);
                            autoCompleteTextFrom.setEnabled(true);
                        }

                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(Transfer.this, R.layout.dropdown_item, accountNames);
                        autoCompleteTextTo.setAdapter(accountAdapter);
                        autoCompleteTextFrom.setAdapter(accountAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Transfer.this, "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(Transfer.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
    private int getAccountLogo(String accountType) {
        Map<String, Integer> accountLogos = new HashMap<>();
        accountLogos.put("Bank Account", R.drawable.bank_logo);
        accountLogos.put("Cash", R.drawable.cash_logo);
        accountLogos.put("Wallet", R.drawable.wallet_logo);
        accountLogos.put("Savings", R.drawable.savings_logo);
        accountLogos.put("Retirement", R.drawable.retirement_logo);
        accountLogos.put("Investment", R.drawable.investment_logo);
        accountLogos.put("Crypto", R.drawable.bitcoin_logo);

        return accountLogos.getOrDefault(accountType, R.drawable.logo);
    }
    private void updateAccountFromLogo(String accountType) {
        accountFromLogo.setImageResource(getAccountLogo(accountType));
    }
    private void updateAccountToLogo(String accountType) {
        accountToLogo.setImageResource(getAccountLogo(accountType));
    }
    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateText.setText(dateFormat.format(calendar.getTime()));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void createTransaction(String userID) {

        // Get transaction type
        String transactionType = "transfer";

        // Get input values
        String transferNameVal = transferNameInput.getText().toString().trim();
        String amountVal = amountInput.getText().toString().trim();
        String noteVal = noteInput.getText().toString().trim();
        String accountFromVal = autoCompleteTextFrom.getText().toString().trim();
        String accountToVal = autoCompleteTextTo.getText().toString().trim();
        String dateVal = dateText.getText().toString().trim();

        // Extract account_type and account_name from selected accountVal (e.g., "Cash | My Wallet")
        String[] accountFromParts = accountFromVal.split(" \\| ");
        String accountTypeFromVal = accountFromParts.length > 0 ? accountFromParts[0].trim() : "";
        String accountNameFromVal = accountFromParts.length > 1 ? accountFromParts[1].trim() : "";

        String[] accountToParts = accountToVal.split(" \\| ");
        String accountTypeToVal = accountToParts.length > 0 ? accountToParts[0].trim() : "";
        String accountNameToVal = accountToParts.length > 1 ? accountToParts[1].trim() : "";

        // Validate input fields
        if (transferNameVal.isEmpty() || amountVal.isEmpty() || accountFromVal.isEmpty() ||
                accountToVal.isEmpty() || dateVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Accounts fields
        if (accountFromVal.equals(accountToVal)) {
            Toast.makeText(this, "Can't transfer on the same account.", Toast.LENGTH_SHORT).show();
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
        StringRequest request = new StringRequest(Request.Method.POST, createTransferURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Transfer Created Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Transfer.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Transactions");
                            startActivity(intent);
                            finish();
                        } else {
                            if (message.toLowerCase().contains("insufficient")) {
                                Toast.makeText(this, "Insufficient balance in selected account.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("TransactionError", "JSON Parsing Error: " + e.getMessage());
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
                params.put("transfer_name", transferNameVal);
                params.put("amount", amountVal);
                params.put("transfer_type", transactionType);
                params.put("transfer_date", dateVal);
                params.put("notes", noteVal);
                params.put("account_type_from", accountTypeFromVal);
                params.put("account_name_from", accountNameFromVal);
                params.put("account_type_to", accountTypeToVal);
                params.put("account_name_to", accountNameToVal);
                return params;
            }
        };
        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}