package com.moneymate.dashboard;

import android.os.Bundle;
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

        // Get budgetID from intent
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
}
