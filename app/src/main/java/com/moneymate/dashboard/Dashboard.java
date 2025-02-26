package com.moneymate.dashboard;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.moneymate.R;

public class Dashboard extends AppCompatActivity {

    // Declare a BottomNavigationView to handle navigation
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Handle system insets
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
                } else if(id == R.id.menuTransactions){
                    fragmentSelected = new TransactionFragment();
                } else if(id == R.id.menuGoals){
                    fragmentSelected = new GoalsFragment();
                } else if(id == R.id.menuReport){
                    fragmentSelected = new ReportFragment();
                } else if(id == R.id.menuProfile){
                    fragmentSelected = new ProfileFragment();
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
    }
}