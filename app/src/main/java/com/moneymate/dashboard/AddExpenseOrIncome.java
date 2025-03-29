package com.moneymate.dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moneymate.R;

public class AddExpenseOrIncome extends AppCompatActivity {
    String[] expenseCategory = {"Bills & Utilities", "Drink & Dine", "Education", "Entertainment",
            "Food & Grocery", "Personal Care", "Pet Care", "Shopping", "Others"};

    String[] incomeCategory = {"Salary & Paycheck", "Business & Profession", "Investments", "Savings",
            "Retirement", "Others"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_expense_or_income);

        TextView title = findViewById(R.id.title);

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the transaction type (income or expense)
        String transactionType = getIntent().getStringExtra("transaction_type");

        // Initialize AutoCompleteTextView
        autoCompleteTextView = findViewById(R.id.autoCompleteText);

        if (transactionType != null) {
            if (transactionType.equals("income")) {
                title.setText("Add Income");
                adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, incomeCategory);
            } else if (transactionType.equals("expense")) {
                title.setText("Add Expense");
                adapterItems = new ArrayAdapter<>(this, R.layout.dropdown_item, expenseCategory);
            }
        }

        // Set adapter to AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapterItems);

        // Use setOnItemClickListener (Fixed method and correct item display)
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item and display it
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(AddExpenseOrIncome.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
