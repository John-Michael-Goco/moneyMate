package com.moneymate.dashboard;

import android.app.DatePickerDialog;
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
import androidx.fragment.app.FragmentTransaction;

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

public class AddExpenseOrIncome extends AppCompatActivity {
    private String fetchAccountsURL = "http://10.0.2.2/moneymateBackend/fetchAccounts.php";
    private String createTransacURL = "http://10.0.2.2/moneymateBackend/createTransaction.php";
    private String[] expenseCategory = {"Bills & Utilities", "Drink & Dine", "Education", "Entertainment",
            "Food & Grocery", "Personal Care", "Pet Care", "Shopping", "Others"};
    private String[] incomeCategory = {"Salary & Paycheck", "Business & Profession", "Investments", "Savings",
            "Retirement", "Others"};
    private ImageView categoryLogo, accountLogo;
    private Button backBtn, createTransacBtn;
    private EditText dateText;
    private TextView title;
    private TextInputEditText transactionNameInput, amountInput, noteTextInput;
    private AutoCompleteTextView autoCompleteTextViewCategory, autoCompleteTextViewAccount;
    private ArrayAdapter<String> adapterItems;
    private ArrayList<String> accountNames = new ArrayList<>();
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense_or_income);

        title = findViewById(R.id.title);
        categoryLogo = findViewById(R.id.categoryLogo);
        accountLogo = findViewById(R.id.accountLogo);
        backBtn = findViewById(R.id.backBtn);
        dateText = findViewById(R.id.dateText);
        transactionNameInput = findViewById(R.id.transactionName);
        amountInput = findViewById(R.id.amountInput);
        noteTextInput = findViewById(R.id.noteText);

        // Set default date (current date)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateText.setText(dateFormat.format(calendar.getTime()));

        // Retrieve userID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get transaction type
        String transactionType = getIntent().getStringExtra("transaction_type");

        // Initialize AutoCompleteTextView
        autoCompleteTextViewCategory = findViewById(R.id.autoCompleteCategoryText);
        autoCompleteTextViewAccount = findViewById(R.id.autoCompleteAccountText);

        if (transactionType != null) {
            if (transactionType.equals("income")) {
                title.setText("Add Income");
                adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, incomeCategory);
            } else if (transactionType.equals("expense")) {
                title.setText("Add Expense");
                adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, expenseCategory);
            }
        }

        // Set adapter for category
        autoCompleteTextViewCategory.setAdapter(adapterItems);

        // Handle selection of category
        autoCompleteTextViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            updateCategoryLogo(selectedCategory);
        });

        // Fetch accounts after setting up categories
        fetchAccounts(userID);

        // Handle account selection
        autoCompleteTextViewAccount.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAccount = parent.getItemAtPosition(position).toString();

            // Extract the account type before the " | " symbol
            String accountType = selectedAccount.split(" \\| ")[0];
            updateAccountLogo(accountType);
        });

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());

        // For date picker
        dateText.setOnClickListener(v -> showDatePicker());

        // Create Transaction
        createTransacBtn = findViewById(R.id.createTransacBtn);
        createTransacBtn.setOnClickListener(v -> createTransaction(userID));
    }

    private void fetchAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(AddExpenseOrIncome.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");

                        accountNames.clear(); // Clear the existing account names

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String accountName = accountObject.getString("account_name");
                            String accountType = accountObject.getString("account_type");

                            // Add the account name to the list
                            accountNames.add(accountType + " | " + accountName);
                        }

                        // Create a new ArrayAdapter for AutoCompleteTextView
                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(AddExpenseOrIncome.this, R.layout.dropdown_item, accountNames);

                        // Set the adapter for AutoCompleteTextView
                        AutoCompleteTextView accountAutoCompleteTextView = findViewById(R.id.autoCompleteAccountText);
                        accountAutoCompleteTextView.setAdapter(accountAdapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddExpenseOrIncome.this, "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AddExpenseOrIncome.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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

        // Update the ImageView if the category exists in the map
        if (categoryLogos.containsKey(category)) {
            categoryLogo.setImageResource(categoryLogos.get(category));
        } else {
            categoryLogo.setImageResource(R.drawable.logo);
        }
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
            dateText.setText(dateFormat.format(calendar.getTime()));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void createTransaction(String userID) {

        // Get transaction type
        String transactionType = getIntent().getStringExtra("transaction_type");

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

        // Create the request to create the transaction
        StringRequest request = new StringRequest(Request.Method.POST, createTransacURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Transaction Created Successfully!", Toast.LENGTH_SHORT).show();
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
                params.put("transaction_name", transactionNameVal);
                params.put("amount", amountVal);
                params.put("category", categoryVal);
                params.put("transaction_type", transactionType);
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
