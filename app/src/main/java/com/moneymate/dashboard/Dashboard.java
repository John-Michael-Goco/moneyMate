package com.moneymate.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import com.moneymate.R;
import com.moneymate.auth.ForgotPasswordCode;
import com.moneymate.auth.ForgotPasswordEmail;

public class Dashboard extends AppCompatActivity {

    private boolean isExpanded = false;

    // Declare a BottomNavigationView to handle navigation
    BottomNavigationView bottomNavigationView;

    // Declare FAB
    private FloatingActionButton floatingButton;

    // Animations for FAB
    private Animation fromBottomFabAnim;
    private Animation toBottomFabAnim;
    private Animation rotateClockWise;
    private Animation rotateCounterClockWise;

    // Main FAB and other views
    private FloatingActionButton addBillFab;
    private FloatingActionButton transferFab;
    private FloatingActionButton addIncomeFab;
    private FloatingActionButton addExpenseFab;

    private View addBillTv;
    private View transferTv;
    private View addIncomeTv;
    private View addExpenseTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Handle system insets, but exclude bottom padding to remove extra whitespace
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Set bottom padding to 0
            return insets;
        });

        // Initialize the BottomNavigationView from the layout
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Initialize FAB
        floatingButton = findViewById(R.id.floatingButton);

        // Initialize animations
        fromBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab);
        toBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab);
        rotateClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise);
        rotateCounterClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_counter_clock_wise);

        // Initialize action FABs and labels
        addBillFab = findViewById(R.id.addBillFab);
        transferFab = findViewById(R.id.transferFab);
        addIncomeFab = findViewById(R.id.addIncomeFab);
        addExpenseFab = findViewById(R.id.addExpenseFab);

        addBillTv = findViewById(R.id.addBillTv);
        transferTv = findViewById(R.id.transferTv);
        addIncomeTv = findViewById(R.id.addIncomeTv);
        addExpenseTv = findViewById(R.id.addExpenseTv);

        // Set click listener to go to Add Income page
        addIncomeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, AddExpenseOrIncome.class);
                intent.putExtra("transaction_type", "income");
                startActivity(intent);
            }
        });

        // Set click listener to go to Add Expense page
        addExpenseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, AddExpenseOrIncome.class);
                intent.putExtra("transaction_type", "expense");
                startActivity(intent);
            }
        });

        // Set click listener to go to Transfer page
        transferFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, Transfer.class);
                startActivity(intent);
            }
        });

        // Set up a listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragmentSelected = null;
                int id = item.getItemId();

                if (id == R.id.menuHome) {
                    fragmentSelected = new HomeFragment();
                    toggleFabVisibility(true);
                } else if (id == R.id.menuBills) {
                    fragmentSelected = new BillsFragment();
                    toggleFabVisibility(true);
                } else if (id == R.id.menuTransactions) {
                    fragmentSelected = new TransactionFragment();
                    toggleFabVisibility(true);
                } else if (id == R.id.menuBudget) {
                    fragmentSelected = new BudgetFragment();
                    toggleFabVisibility(false); // Hide FAB
                } else if (id == R.id.menuAccount) {
                    fragmentSelected = new AccountFragment();
                    toggleFabVisibility(false); // Hide FAB
                }

                // Replace the current fragment with the new one
                if (fragmentSelected != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragmentSelected).commit();
                }
                return true;
            }
        });

        // Load the HomeFragment by default and show FAB
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
            toggleFabVisibility(true);
        }

        // Toggle FAB menu
        floatingButton.setOnClickListener(v -> {
            isExpanded = !isExpanded;

            // Animation for Main FAB
            floatingButton.startAnimation(isExpanded ? rotateClockWise : rotateCounterClockWise);

            if (isExpanded) {
                // Show action buttons and labels with animation
                showFab(addBillFab, addBillTv);
                showFab(transferFab, transferTv);
                showFab(addIncomeFab, addIncomeTv);
                showFab(addExpenseFab, addExpenseTv);
            } else {
                // Hide action buttons and labels with animation
                hideFab(addBillFab, addBillTv);
                hideFab(transferFab, transferTv);
                hideFab(addIncomeFab, addIncomeTv);
                hideFab(addExpenseFab, addExpenseTv);
            }
        });
    }

    // Function to show FAB and label
    private void showFab(View fab, View label) {
        fab.setVisibility(View.VISIBLE);
        fab.setClickable(true);
        fab.startAnimation(fromBottomFabAnim);

        label.setVisibility(View.VISIBLE);
        label.setClickable(true);
        label.startAnimation(fromBottomFabAnim);
    }

    // Function to hide FAB and label
    private void hideFab(View fab, View label) {
        fab.startAnimation(toBottomFabAnim);
        fab.setVisibility(View.GONE);
        fab.setClickable(false);

        label.startAnimation(toBottomFabAnim);
        label.setVisibility(View.GONE);
        label.setClickable(false);
    }

    // Function for Toggle FAB visibility from Fragments
    public void toggleFabVisibility(boolean isVisible) {
        if (isVisible) {
            floatingButton.show();
        } else {
            floatingButton.hide();

            // Reset FAB menu
            if (isExpanded) {
                isExpanded = false;
                hideFab(addBillFab, addBillTv);
                hideFab(transferFab, transferTv);
                hideFab(addIncomeFab, addIncomeTv);
                hideFab(addExpenseFab, addExpenseTv);
                floatingButton.startAnimation(rotateCounterClockWise);
            }
        }
    }
}
