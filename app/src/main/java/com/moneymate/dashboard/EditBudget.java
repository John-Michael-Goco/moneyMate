package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditBudget extends AppCompatActivity {
    private static final String fetchBudgetDetailsURL = "http://10.0.2.2/moneymateBackend/fetchBudgetDetails.php";
    private static final String updateBudgetURL = "http://10.0.2.2/moneymateBackend/updateBudget.php";

    private String[] expenseCategory = {"Bills & Utilities", "Drink & Dine", "Education", "Entertainment",
            "Food & Grocery", "Personal Care", "Pet Care", "Shopping", "Others"};

    private ImageView categoryLogo;
    private Button backBtn, updateBtn;
    private TextInputEditText budgetNameInput, amountInput;
    private TextView createdAtText;
    private AutoCompleteTextView autoCompleteTextViewCategory;
    private ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_budget);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch necessary IDs
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");
        String budgetID = getIntent().getStringExtra("budgetID");

        // Find UI
        categoryLogo = findViewById(R.id.categoryLogo);
        backBtn = findViewById(R.id.backBtn);
        updateBtn = findViewById(R.id.updateBtn);
        budgetNameInput = findViewById(R.id.budgetNameInput);
        amountInput = findViewById(R.id.amountInput);
        createdAtText = findViewById(R.id.createdAtText);

        // Initialize AutoCompleteTextView
        autoCompleteTextViewCategory = findViewById(R.id.autoCompleteCategoryText);
        adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, expenseCategory);
        autoCompleteTextViewCategory.setAdapter(adapterItems);

        // Handle selection of category
        autoCompleteTextViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            updateCategoryLogo(selectedCategory);
        });

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());

        // Handle Update Button
        updateBtn.setOnClickListener(v -> {
            showConfirmationDialog(budgetID, userID);
            updateBtn.setEnabled(false);
            updateBtn.postDelayed(() -> updateBtn.setEnabled(true), 1500);
        });

        // ✅ Fetch and display existing budget details
        getBudgetDetails(budgetID);
    }
    private void getBudgetDetails(String budget_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(EditBudget.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchBudgetDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject budgetObject = jsonResponse.getJSONObject("budgetDetails");
                            String budget_name = budgetObject.getString("budget_name");
                            String category = budgetObject.getString("category");
                            double amount = Double.parseDouble(budgetObject.getString("amount"));
                            String createdAt = budgetObject.getString("created_at");

                            // Format Created At to "Month Year"
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                            Date date = inputFormat.parse(createdAt);
                            String formattedDate = outputFormat.format(date);

                            // Populate the UI
                            budgetNameInput.setText(budget_name);
                            amountInput.setText(String.valueOf(amount));
                            createdAtText.setText("Created on: " + formattedDate);
                            autoCompleteTextViewCategory.setText(category, false);
                            updateCategoryLogo(category);

                        } else {
                            Toast.makeText(EditBudget.this, "Failed to fetch budget details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditBudget.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(EditBudget.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("budgetID", budget_id);
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

        // Update the ImageView
        categoryLogo.setImageResource(categoryLogos.getOrDefault(category, R.drawable.logo));
    }
    private void showConfirmationDialog(String budgetID, String userID) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Update")
                .setMessage("Are you sure you want to update this budget?")
                .setPositiveButton("Yes", (dialog, which) -> updateBudget(budgetID, userID))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void updateBudget(String budgetID, String userID) {

        // Get input values
        String budgetNameVal = budgetNameInput.getText().toString().trim();
        String amountVal = amountInput.getText().toString().trim();
        String categoryVal = autoCompleteTextViewCategory.getText().toString().trim();

        // Validate input fields
        if (budgetNameVal.isEmpty() || amountVal.isEmpty() || categoryVal.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Amount
        try {
            double amount = Double.parseDouble(amountVal);
            if (amount <= 0) {
                Toast.makeText(EditBudget.this, "Enter a valid positive amount.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(EditBudget.this, "Amount must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to create the transaction
        StringRequest request = new StringRequest(Request.Method.POST, updateBudgetURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(EditBudget.this, "Budget Updated Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditBudget.this, ViewBudget.class);
                            intent.putExtra("budgetID", budgetID);
                            startActivity(intent);
                            finish();
                        } else if ("exists".equals(status)) {
                            Toast.makeText(EditBudget.this, "Already have a budget with " + categoryVal + ".", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EditBudget.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("BudgetError", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(EditBudget.this, "Parsing Error. Try again later.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request Error: " + error.toString());
                    Toast.makeText(EditBudget.this, "Request Error. Check your connection.", Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("budgetID", budgetID);
                params.put("budget_name", budgetNameVal);
                params.put("amount", amountVal);
                params.put("category", categoryVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
