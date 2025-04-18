package com.moneymate.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewBudget extends AppCompatActivity {
    private static final String fetchBudgetDetailsURL = "http://10.0.2.2/moneymateBackend/fetchBudgetDetails.php";
    private static final String deleteBudgetURL = "http://10.0.2.2/moneymateBackend/deleteBudget.php";
    private ImageView backBtn, editBtn, deleteBtn, budgetLogo;
    private TextView budgetNameText, categoryText, createdAtText, progressText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_budget);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get budgetID from intent
        String budgetID = getIntent().getStringExtra("budgetID");

        // UI references
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        budgetLogo = findViewById(R.id.budgetLogo);
        budgetNameText = findViewById(R.id.budgetNameText);
        categoryText = findViewById(R.id.categoryText);
        createdAtText = findViewById(R.id.createdAtText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());

        // Handle Edit Button
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewBudget.this, EditBudget.class);
            intent.putExtra("budgetID", budgetID);
            startActivity(intent);
            finish();
        });

        // Handle Edit Button
        deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog(budgetID));

        // Fetch data
        getBudgetDetails(budgetID);
    }

    private void getBudgetDetails(String budget_id) {
        RequestQueue requestQueue = Volley.newRequestQueue(ViewBudget.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchBudgetDetailsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject budgetObject = jsonResponse.getJSONObject("budgetDetails");
                            String name = budgetObject.getString("budget_name");
                            String category = budgetObject.getString("category");
                            double amount = Double.parseDouble(budgetObject.getString("amount"));
                            String createdAt = budgetObject.getString("created_at");
                            double totalSpent = Double.parseDouble(budgetObject.optString("total_spent", "0"));

                            // Set UI values
                            budgetNameText.setText(name);
                            categoryText.setText(category);

                            // Format Created at Like MM/YYYY
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

                            Date date = inputFormat.parse(createdAt);
                            String formattedDate = outputFormat.format(date);

                            createdAtText.setText("Created on: " + formattedDate);
                            budgetLogo.setImageResource(getCategoryLogo(category));

                            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
                            progressText.setText(currency.format(totalSpent) + " of " + currency.format(amount));

                            int progress = (int) ((totalSpent / amount) * 100);
                            if (progress > 100) progress = 100;
                            progressBar.setProgress(progress);

                            if (totalSpent > amount) {
                                progressBar.setProgressTintList(getResources().getColorStateList(R.color.red, null));
                            } else {
                                progressBar.setProgressTintList(getResources().getColorStateList(R.color.green, null));
                            }

                        } else {
                            Toast.makeText(ViewBudget.this, "Failed to fetch budget details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewBudget.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ViewBudget.this, "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
        logos.put("Others", R.drawable.others_ic);

        return logos.getOrDefault(category, R.drawable.logo);
    }
    private void showDeleteConfirmationDialog(String budgetID) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteBudget(budgetID);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deleteBudget(String budget_id) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteBudgetURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Budget deleted successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ViewBudget.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Budget");
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Failed to delete budget.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(this, "Error deleting budget.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("budgetID", budget_id);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
