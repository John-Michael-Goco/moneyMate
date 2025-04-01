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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.moneymate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditAccount extends AppCompatActivity {

    private static final String fetchAccountDetails = "http://192.168.1.6/moneymateBackend/fetchAccountDetails.php";
    private static final String updateAccount = "http://192.168.1.6/moneymateBackend/updateAccount.php";
    private Button backBtn, updateAccountBtn;
    private TextInputEditText accountTypeText, accountNameText, accountNumberText, balanceText;
    private ImageView accountTypeLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_account);

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

        // Handle update button
        updateAccountBtn = findViewById(R.id.updateAccountBtn);
        updateAccountBtn.setOnClickListener(v -> showConfirmationDialog(accountID));
    }

    private void showConfirmationDialog(String accountID) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update your details?")
                .setPositiveButton("Yes", (dialog, which) -> updateAccountDetails(accountID))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void getAccountDetails(String accountID) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditAccount.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAccountDetails,
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
                            accountTypeText = findViewById(R.id.accountTypeText);
                            accountNameText = findViewById(R.id.accountNameText);
                            accountNumberText = findViewById(R.id.accountNumberText);
                            balanceText = findViewById(R.id.balanceText);
                            accountTypeLogo = findViewById(R.id.accountNameLogo);

                            // Convert the string logo ID to an integer resource ID
                            int accountLogoResID = getResources().getIdentifier(account_logo, "drawable", getPackageName());

                            accountNameText.setText(account_name);
                            accountTypeText.setText(account_type);
                            accountNumberText.setText(account_number);
                            accountTypeLogo.setImageResource(accountLogoResID);
                            balanceText.setText(balance);

                        } else {
                            Toast.makeText(this, "Failed to fetch account details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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

    private void updateAccountDetails(String accountID) {

        // Get input values
        String accountNameVal = accountNameText.getText().toString().trim();
        String accountNumberVal = accountNumberText.getText().toString().trim();
        String balanceVal = balanceText.getText().toString().trim();

        // Input validation
        if (accountNameVal.isEmpty() || accountNumberVal.isEmpty() || balanceVal.isEmpty()) {
            Toast.makeText(EditAccount.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to update the student
        StringRequest request = new StringRequest(Request.Method.POST, updateAccount,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(EditAccount.this, "Account Successfully Updated!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditAccount.this, ViewAccount.class);
                            intent.putExtra("accountID", accountID);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EditAccount.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("UpdateError", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(EditAccount.this, "Parsing Error. Try again later.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request Error: " + error.toString());
                    Toast.makeText(EditAccount.this, "Request Error. Check your connection.", Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountID", accountID);
                params.put("account_name", accountNameVal);
                params.put("account_number", accountNumberVal);
                params.put("balance", balanceVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}