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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateBudget extends AppCompatActivity {
    private String createBudgetURL = "http://10.0.2.2/moneymateBackend/createBudget.php";
    private String[] expenseCategory = {"Bills & Utilities", "Drink & Dine", "Education", "Entertainment",
            "Food & Grocery", "Personal Care", "Pet Care", "Shopping"};
    private ImageView categoryLogo;
    private Button backBtn, createBudgetBtn;
    private TextInputEditText budgetNameInput, amountInput;
    private AutoCompleteTextView autoCompleteTextViewCategory;
    private ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_budget);

        // Retrieve userID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryLogo = findViewById(R.id.categoryLogo);
        backBtn = findViewById(R.id.backBtn);
        createBudgetBtn = findViewById(R.id.createBudgetBtn);
        budgetNameInput = findViewById(R.id.budgetNameInput);
        amountInput = findViewById(R.id.amountInput);

        // Initialize AutoCompleteTextView
        autoCompleteTextViewCategory = findViewById(R.id.autoCompleteCategoryText);
        adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, expenseCategory);

        // Set adapter for category
        autoCompleteTextViewCategory.setAdapter(adapterItems);

        // Handle selection of category
        autoCompleteTextViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            updateCategoryLogo(selectedCategory);
        });

        // Handle Back Button
        backBtn.setOnClickListener(v -> finish());

        // Create Budget
        createBudgetBtn.setOnClickListener(v -> {
            createBudget(userID);
            createBudgetBtn.setEnabled(false);
            createBudgetBtn.postDelayed(() -> createBudgetBtn.setEnabled(true), 1500);
        });
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

        // Update the ImageView if the category exists in the map
        if (categoryLogos.containsKey(category)) {
            categoryLogo.setImageResource(categoryLogos.get(category));
        } else {
            categoryLogo.setImageResource(R.drawable.logo);
        }
    }
    private void createBudget(String userID) {

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
                Toast.makeText(this, "Enter a valid positive amount.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the request to create the transaction
        StringRequest request = new StringRequest(Request.Method.POST, createBudgetURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "Transaction Created Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CreateBudget.this, Dashboard.class);
                            intent.putExtra("fragmentToOpen", "Budget");
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("BudgetError", "JSON Parsing Error: " + e.getMessage());
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
                params.put("budget_name", budgetNameVal);
                params.put("category", categoryVal);
                params.put("amount", amountVal);
                return params;
            }
        };

        // Add the request to the Volley queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}