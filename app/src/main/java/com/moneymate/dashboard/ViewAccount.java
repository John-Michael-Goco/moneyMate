package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moneymate.R;
import com.moneymate.adapters.TransactionsAdapter;
import com.moneymate.models.CashModel;
import com.moneymate.models.TransactionsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewAccount extends AppCompatActivity {

    private static final String fetchAccountDetailsURL = "http://10.0.2.2/moneymateBackend/fetchAccountDetails.php";
    private static final String fetchTransactionsURL = "http://10.0.2.2/moneymateBackend/fetchTransactions.php";
    private static final String deleteAccountURL = "http://10.0.2.2/moneymateBackend/deleteAccount.php";
    private ImageView backBtn, updateBtn, deleteBtn;
    private RecyclerView transactionsRecyclerView;
    private List<TransactionsModel> transactionsList = new ArrayList<>();
    private TransactionsAdapter transactionsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the account ID from intent and fetch details
        String accountID = getIntent().getStringExtra("accountID");
        getAccountDetails(accountID);

        // Handle back button
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        updateBtn = findViewById(R.id.editBtn);
        updateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewAccount.this, EditAccount.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        });

        // Handle Delete Button
        deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> showConfirmationDialog(accountID));

        // For Transactions RV
        transactionsRecyclerView = findViewById(R.id.transactionsRV);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsAdapter = new TransactionsAdapter(this, transactionsList);
        transactionsRecyclerView.setAdapter(transactionsAdapter);

        // Retrieve user data
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        // Fetch Transactions
        fetchTransactions(userID, accountID);
    }
    private void showConfirmationDialog(String accountID) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAccount(accountID))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void getAccountDetails(String accountID) {
        RequestQueue requestQueue = Volley.newRequestQueue(ViewAccount.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject accountObject = jsonResponse.getJSONObject("accountDetails");
                            String account_type = accountObject.getString("account_type");
                            String account_logo = accountObject.getString("account_logo");
                            String account_name = accountObject.getString("account_name");
                            String account_number = accountObject.getString("account_number");
                            String balance = accountObject.getString("balance");

                            // Update UI
                            TextView accountNameText = findViewById(R.id.accountNameText);
                            TextView accountText = findViewById(R.id.accountText);
                            TextView accountNumberText = findViewById(R.id.accountNumberText);
                            TextView balanceText = findViewById(R.id.balanceText);
                            ImageView accountLogo = findViewById(R.id.accountLogo);

                            // Convert the string logo ID to an integer resource ID
                            int accountLogoResID = getResources().getIdentifier(account_logo, "drawable", getPackageName());

                            accountNameText.setText(account_name);
                            accountText.setText(account_type);
                            accountNumberText.setText(account_number);
                            accountLogo.setImageResource(accountLogoResID);

                            // Convert balance to a formatted currency string
                            double balanceValue = Double.parseDouble(balance);
                            NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
                            currencyFormat.setMinimumFractionDigits(2);
                            currencyFormat.setMaximumFractionDigits(2);
                            String formattedBalance = "₱ " + currencyFormat.format(balanceValue);

                            balanceText.setText(formattedBalance);

                        } else {
                            Toast.makeText(ViewAccount.this, "Failed to fetch account details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewAccount.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ViewAccount.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountID", accountID);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
    private void deleteAccount(String accountID) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteAccountURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragmentContainer, new AccountFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(this, "Error deleting account.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountID", accountID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void fetchTransactions(String userID, String accountID) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchTransactionsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("transactions");
                        transactionsList.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String transactionID = accountObject.getString("transactionID");
                            String transaction_name = accountObject.getString("transaction_name");
                            String amount = accountObject.getString("amount");
                            String category = accountObject.getString("category");
                            String transactionType = accountObject.getString("transaction_type");
                            String transactionDate = accountObject.getString("transaction_date");

                            transactionsList.add(new TransactionsModel(transactionID, transaction_name, amount, category, transactionType, transactionDate));
                        }
                        transactionsAdapter.notifyDataSetChanged();

                        // Show/hide "No accounts yet" message
                        TextView noTransaction = this.findViewById(R.id.noTransaction);
                        noTransaction.setVisibility(transactionsList.isEmpty() ? View.VISIBLE : View.GONE);
                        transactionsRecyclerView.setVisibility(transactionsList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load transactions. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("accountID", accountID);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}