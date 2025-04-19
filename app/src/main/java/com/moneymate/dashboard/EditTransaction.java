package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditTransaction extends AppCompatActivity {

    private final String fetchAccountsURL = "http://10.0.2.2/moneymateBackend/fetchAccounts.php";
    private final String fetchTransactionDetailsURL = "http://10.0.2.2/moneymateBackend/fetchTransactionDetails.php";
    private final String updateTransactionURL = "http://10.0.2.2/moneymateBackend/updateTransaction.php";
    private final String[] expenseCategory = {"Bills & Utilities", "Drink & Dine", "Education", "Entertainment", "Food & Grocery", "Personal Care", "Pet Care", "Shopping", "Others"};
    private final String[] incomeCategory = {"Salary & Paycheck", "Business & Profession", "Investments", "Savings", "Retirement", "Others"};

    private ImageView categoryLogo, accountLogo;
    private Button backBtn, updateBtn;
    private EditText dateText;
    private TextView title;
    private TextInputEditText transactionNameInput, amountInput, noteTextInput;
    private AutoCompleteTextView autoCompleteTextViewCategory, autoCompleteTextViewAccount;
    private ArrayAdapter<String> adapterItems;
    private final ArrayList<String> accountNames = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();
    private String userID, transactionID, accountID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        title = findViewById(R.id.title);
        categoryLogo = findViewById(R.id.categoryLogo);
        accountLogo = findViewById(R.id.accountLogo);
        dateText = findViewById(R.id.dateText);
        transactionNameInput = findViewById(R.id.transactionName);
        amountInput = findViewById(R.id.amountInput);
        noteTextInput = findViewById(R.id.noteText);
        backBtn = findViewById(R.id.backBtn);
        updateBtn = findViewById(R.id.updateBtn);

        autoCompleteTextViewCategory = findViewById(R.id.autoCompleteCategoryText);
        autoCompleteTextViewAccount = findViewById(R.id.autoCompleteAccountText);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "1");
        transactionID = getIntent().getStringExtra("transactionID");
        accountID = getIntent().getStringExtra("accountID");

        autoCompleteTextViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            updateCategoryLogo(selectedCategory);
        });

        autoCompleteTextViewAccount.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAccount = parent.getItemAtPosition(position).toString();
            String accountType = selectedAccount.split(" \\| ")[0];
            updateAccountLogo(accountType);
        });

        dateText.setOnClickListener(v -> showDatePicker());

        // Fetch accounts for the dropdown
        fetchAccounts(userID);
        // Fetch transaction details
        getTransactionDetails(transactionID);
        // Handle Update Button
        updateBtn.setOnClickListener(v -> showConfirmationDialog(transactionID, userID, accountID));
        // Handle Back Button
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditTransaction.this, ViewTransaction.class);
            intent.putExtra("accountID", accountID);
            intent.putExtra("transactionID", transactionID);
            startActivity(intent);
            finish();
        });
    }
    private void fetchAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditTransaction.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");

                        accountNames.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String accountName = accountObject.getString("account_name");
                            String accountType = accountObject.getString("account_type");

                            accountNames.add(accountType + " | " + accountName);
                        }

                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(EditTransaction.this, R.layout.dropdown_item, accountNames);
                        autoCompleteTextViewAccount.setAdapter(accountAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditTransaction.this, "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(EditTransaction.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
    private void getTransactionDetails(String transactionID) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditTransaction.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchTransactionDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject accountObject = jsonResponse.getJSONObject("transactionDetails");
                            String transaction_name = accountObject.getString("transaction_name");
                            String amount = accountObject.getString("amount");
                            String category = accountObject.getString("category");
                            String transaction_type = accountObject.getString("transaction_type");
                            String transaction_date = accountObject.getString("transaction_date");
                            String notes = accountObject.getString("notes");
                            String account_name = accountObject.getString("account_name");
                            String account_type = accountObject.getString("account_type");

                            // Set fields
                            transactionNameInput.setText(transaction_name);
                            dateText.setText(transaction_date);
                            noteTextInput.setText(notes);
                            autoCompleteTextViewAccount.setText(account_type + " | " + account_name, false);

                            autoCompleteTextViewCategory.setText(category);
                            amountInput.setText(amount);

                            // Set appropriate category list
                            if (transaction_type != null) {
                                if (transaction_type.equals("income")) {
                                    title.setText("Edit Income");
                                    adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, incomeCategory);
                                } else if (transaction_type.equals("expense")) {
                                    title.setText("Edit Expense");
                                    adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, expenseCategory);
                                }
                            }

                            autoCompleteTextViewCategory.setAdapter(adapterItems);
                            updateCategoryLogo(category);
                            updateAccountLogo(account_type);

                        } else {
                            Toast.makeText(EditTransaction.this, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditTransaction.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(EditTransaction.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("transactionID", transactionID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
    private void showConfirmationDialog(String transactionID, String userID, String accountID) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update this transaction?")
                .setPositiveButton("Yes", (dialog, which) -> updateTransaction(transactionID, userID, accountID))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void updateCategoryLogo(String category) {
        Map<String, Integer> categoryLogos = new HashMap<>();
        categoryLogos.put("Bills & Utilities", R.drawable.bills_ic);
        categoryLogos.put("Drink & Dine", R.drawable.drink_ic);
        categoryLogos.put("Education", R.drawable.education_ic);
        categoryLogos.put("Entertainment", R.drawable.entertainment_ic);
        categoryLogos.put("Food & Grocery", R.drawable.food_ic);
        categoryLogos.put("Personal Care", R.drawable.personal_care_ic);
        categoryLogos.put("Pet Care", R.drawable.pet_care_ic);
        categoryLogos.put("Shopping", R.drawable.shopping_ic);
        categoryLogos.put("Others", R.drawable.others_ic);
        categoryLogos.put("Salary & Paycheck", R.drawable.salary_ic);
        categoryLogos.put("Business & Profession", R.drawable.business_ic);
        categoryLogos.put("Investments", R.drawable.investment_ic);
        categoryLogos.put("Savings", R.drawable.salary_ic);
        categoryLogos.put("Retirement", R.drawable.retirement_ic);

        categoryLogo.setImageResource(categoryLogos.getOrDefault(category, R.drawable.logo));
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

        accountLogo.setImageResource(accountLogos.getOrDefault(accountName, R.drawable.logo));
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
    private void updateTransaction(String transactionID, String userID, String accountID) {

        // Get input values
        String transactionNameVal = transactionNameInput.getText().toString().trim();
        String amountVal = amountInput.getText().toString().trim();
        String noteTextVal = noteTextInput.getText().toString().trim();
        String categoryVal = autoCompleteTextViewCategory.getText().toString().trim();
        String accountVal = autoCompleteTextViewAccount.getText().toString().trim();
        String dateVal = dateText.getText().toString().trim();

        // Extract account_type and account_name from selected accountVal (e.g., "Cash | My Wallet")
        String[] accountParts = accountVal.split(" \\| ");
        String accountTypeVal = accountParts.length > 0 ? accountParts[0].trim() : "";
        String accountNameVal = accountParts.length > 1 ? accountParts[1].trim() : "";

        // Validate input fields
        if (transactionNameVal.isEmpty() || amountVal.isEmpty() || categoryVal.isEmpty() ||
                accountVal.isEmpty() || dateVal.isEmpty()) {
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
        StringRequest request = new StringRequest(Request.Method.POST, updateTransactionURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Transaction Updated Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditTransaction.this, ViewTransaction.class);
                            intent.putExtra("transactionID", transactionID);
                            intent.putExtra("accountID", accountID);
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
                params.put("transactionID", transactionID);
                params.put("transaction_name", transactionNameVal);
                params.put("amount", amountVal);
                params.put("category", categoryVal);
                params.put("transaction_date", dateVal);
                params.put("notes", noteTextVal);
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
