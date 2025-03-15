package com.moneymate.dashboard;

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

public class Dashboard extends AppCompatActivity {

    private boolean isExpanded = false;

    // Declare a BottomNavigationView to handle navigation
    BottomNavigationView bottomNavigationView;

    // Animations for FAB
    private Animation fromBottomFabAnim;
    private Animation toBottomFabAnim;
    private Animation rotateClockWise;
    private Animation rotateCounterClockWise;

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

        // Set up a listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // Determine which menu item was selected and assign the appropriate fragment
                Fragment fragmentSelected = null;

                int id = item.getItemId();

                if(id == R.id.menuHome){
                    fragmentSelected = new HomeFragment();
                }else if(id == R.id.menuBills){
                    fragmentSelected = new BillsFragment();
                }  else if(id == R.id.menuTransactions){
                    fragmentSelected = new TransactionFragment();
                } else if(id == R.id.menuBudget){
                    fragmentSelected = new BudgetFragment();
                } else if(id == R.id.menuAccount){
                    fragmentSelected = new AccountFragment();
                }

                // Replace the current fragment with the new one
                if(fragmentSelected != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragmentSelected).commit();
                }
                return true;
            }
        });

        // load the HomeFragment by default
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        }

        // Initialize animations
        fromBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab);
        toBottomFabAnim = AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab);
        rotateClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise);
        rotateCounterClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_counter_clock_wise);

        // Main FAB and other views
        FloatingActionButton floatingButton = findViewById(R.id.floatingButton);

        FloatingActionButton addBillFab = findViewById(R.id.addBillFab);
        FloatingActionButton transferFab = findViewById(R.id.transferFab);
        FloatingActionButton addIncomeFab = findViewById(R.id.addIncomeFab);
        FloatingActionButton addExpenseFab = findViewById(R.id.addExpenseFab);

        View addBillTv = findViewById(R.id.addBillTv);
        View transferTv = findViewById(R.id.transferTv);
        View addIncomeTv = findViewById(R.id.addIncomeTv);
        View addExpenseTv = findViewById(R.id.addExpenseTv);

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
    // Helper method to show FAB and label
    private void showFab(View fab, View label) {
        fab.setVisibility(View.VISIBLE);
        fab.setClickable(true);
        fab.startAnimation(fromBottomFabAnim);

        label.setVisibility(View.VISIBLE);
        label.setClickable(true);
        label.startAnimation(fromBottomFabAnim);
    }

    // Helper method to hide FAB and label
    private void hideFab(View fab, View label) {
        fab.startAnimation(toBottomFabAnim);
        fab.setVisibility(View.GONE);
        fab.setClickable(false);

        label.startAnimation(toBottomFabAnim);
        label.setVisibility(View.GONE);
        label.setClickable(false);
    }
}