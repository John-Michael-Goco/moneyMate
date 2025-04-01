package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.moneymate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewAccount extends AppCompatActivity {

    private static final String fetchAccountDetailsURL = "http://192.168.1.6/moneymateBackend/fetchAccountDetails.php";
    private static final String deleteAccountURL = "http://192.168.1.6/moneymateBackend/deleteAccount.php";

    private ImageView backBtn, updateBtn, deleteBtn;

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

        deleteBtn = findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> showConfirmationDialog(accountID));
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
                            Toast.makeText(this, "Student deleted successfully!", Toast.LENGTH_SHORT).show();
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragmentContainer, new AccountFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        } else {
                            Toast.makeText(this, "Failed to delete student.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(this, "Error deleting student.", Toast.LENGTH_SHORT).show();
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
}