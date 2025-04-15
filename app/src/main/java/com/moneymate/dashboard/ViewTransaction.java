package com.moneymate.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.moneymate.auth.ForgotPasswordEmail;
import com.moneymate.auth.NewPassword;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewTransaction extends AppCompatActivity {
    private static final String fetchTransactionDataURL = "http://10.0.2.2/moneymateBackend/fetchTransactionDetails.php";
    private static final String deleteTransactionURL = "http://10.0.2.2/moneymateBackend/deleteTransaction.php";
    private ImageView backBtn, editBtn, deleteBtn, categoryLogo;
    private TextView account, transactionName, dateText, amountText, categoryTypeText, categoryText, notesText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_transaction);

        account = findViewById(R.id.account);
        transactionName = findViewById(R.id.transactionName);
        dateText = findViewById(R.id.dateText);
        amountText = findViewById(R.id.amountText);
        categoryTypeText = findViewById(R.id.categoryTypeText);
        categoryText = findViewById(R.id.categoryText);
        notesText = findViewById(R.id.notesText);
        categoryLogo = findViewById(R.id.categoryLogo);
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the IDs from intent and fetch Transaction Data
        String transactionID = getIntent().getStringExtra("transactionID");
        String accountID = getIntent().getStringExtra("accountID");
        getTransactionDetails(transactionID, accountID);

        // Handle Back Button
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewTransaction.this, ViewAccount.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
            finish();
        });

        // Handle Delete Button
        deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog(transactionID, accountID));

        // Handle Edit Button
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewTransaction.this, EditTransaction.class);
            intent.putExtra("transactionID", transactionID);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
            finish();
        });
    }
    private void showDeleteConfirmationDialog(String transactionID, String accountID) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteTransaction(transactionID, accountID);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void getTransactionDetails(String transactionID, String accountID) {
        RequestQueue requestQueue = Volley.newRequestQueue(ViewTransaction.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchTransactionDataURL,
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

                            // Format and set amount
                            double amountValue = Double.parseDouble(amount);
                            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                            String formattedAmount = formatter.format(amountValue);
                            amountText.setText(formattedAmount);

                            // Set color based on transaction type
                            if (transaction_type.equalsIgnoreCase("income")) {
                                amountText.setTextColor(getResources().getColor(R.color.green, null));
                            } else if (transaction_type.equalsIgnoreCase("expense")) {
                                amountText.setTextColor(getResources().getColor(R.color.red, null));
                            }

                            // Capitalize transaction type
                            String capitalizedType = transaction_type.substring(0, 1).toUpperCase() + transaction_type.substring(1).toLowerCase();

                            // Format date
                            String formattedDate = transaction_date; // default
                            try {
                                java.text.SimpleDateFormat originalFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                                java.util.Date date = originalFormat.parse(transaction_date);
                                if (date != null) {
                                    formattedDate = displayFormat.format(date);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Update UI
                            account.setText(account_type + " | " + account_name);
                            transactionName.setText(transaction_name);
                            dateText.setText(formattedDate);
                            categoryTypeText.setText(capitalizedType);
                            categoryText.setText(category);
                            notesText.setText(notes);

                            int logoResId = getCategoryLogo(category);
                            categoryLogo.setImageResource(logoResId);

                            // Handle Edit Btn
                            if (category.equalsIgnoreCase("Transfer")) {
                                editBtn.setVisibility(View.INVISIBLE);
                                deleteBtn.setVisibility(View.INVISIBLE);
                            } else {
                                editBtn.setVisibility(View.VISIBLE);
                                deleteBtn.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(ViewTransaction.this, "Failed to fetch transaction details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewTransaction.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ViewTransaction.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
    private int getCategoryLogo(String category) {
        Map<String, Integer> logos = new HashMap<>();
        logos.put("Bills & Utilities", R.drawable.bills_ic);
        logos.put("Drink & Dine", R.drawable.drink_ic);
        logos.put("Education", R.drawable.education_ic);
        logos.put("Entertainment", R.drawable.entertainment_ic);
        logos.put("Food & Grocery", R.drawable.food_ic);
        logos.put("Personal Care", R.drawable.personal_care_ic);
        logos.put("Pet Care", R.drawable.pet_care_ic);
        logos.put("Shopping", R.drawable.shopping_ic);
        logos.put("Salary & Paycheck", R.drawable.salary_ic);
        logos.put("Business & Profession", R.drawable.business_ic);
        logos.put("Investments", R.drawable.investment_ic);
        logos.put("Savings", R.drawable.salary_ic);
        logos.put("Retirement", R.drawable.retirement_ic);
        logos.put("Others", R.drawable.others_ic);
        logos.put("Transfer", R.drawable.transfer);

        return logos.getOrDefault(category, R.drawable.logo);
    }
    private void deleteTransaction(String transactionID, String accountID) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteTransactionURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Transaction deleted successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ViewTransaction.this, ViewAccount.class);
                            intent.putExtra("accountID", accountID);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete transaction.", Toast.LENGTH_SHORT).show();
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
                params.put("transactionID", transactionID);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}