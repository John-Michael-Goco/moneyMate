package com.moneymate.dashboard;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.moneymate.R;

public class CategoryViewPager extends AppCompatActivity {


    private final String[] titles = {"Expense / Bills", "Income"};

    private final String[][] categoryNames = {
            {
                    "Bills & Utilities", "Drink & Dine", "Education",
                    "Entertainment", "Food & Grocery", "Personal Care",
                    "Pet Care", "Shopping", "Others"
            },
            {
                    "Salary & Paycheck", "Business & Profession",
                    "Investments", "Savings", "Retirement", "Others"
            }
    };

    private final int[][] categoryImages = {
            {
                    R.drawable.bills_ic, R.drawable.drink_ic, R.drawable.education_ic,
                    R.drawable.entertainment_ic, R.drawable.food_ic, R.drawable.personal_care_ic,
                    R.drawable.pet_care_ic, R.drawable.shopping_ic, R.drawable.others_ic
            },
            {
                    R.drawable.salary_ic, R.drawable.business_ic, R.drawable.investment_ic,
                    R.drawable.savings_ic, R.drawable.retirement_ic, R.drawable.others_ic
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_view_pager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewPager2 sampleViewPager = findViewById(R.id.viewPager);
        sampleViewPager.setAdapter(new SampleAdapter(this));

        TabLayout sampleTabLayout = findViewById(R.id.tabLayout);

        new TabLayoutMediator(sampleTabLayout, sampleViewPager, (tab, position) -> {
            tab.setText(titles[position]);
        }).attach();

        LinearLayout backBtn = findViewById(R.id.backButton);

        // Handle back button
        backBtn.setOnClickListener(v -> finish());
    }

    class SampleAdapter extends FragmentStateAdapter {

        public SampleAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new CategoryFragment(categoryNames[position], categoryImages[position]);
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }
}
