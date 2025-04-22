package com.moneymate.dashboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.moneymate.R;
import com.moneymate.adapters.TransactionsPagerAdapter;

public class TransactionFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Updated to handle 4 tabs
        TransactionsPagerAdapter adapter = new TransactionsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        String[] tabTitles = {"Expenses", "Incomes", "Transfer", "Monthly"};

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position < tabTitles.length) {
                tab.setText(tabTitles[position]);
            }
        }).attach();

        // 👇 Check if there's an argument and switch to the right tab
        String tabToOpen = getArguments() != null ? getArguments().getString("tabToOpen") : null;

        if (tabToOpen != null) {
            if (tabToOpen.equalsIgnoreCase("Incomes")) {
                viewPager.setCurrentItem(1, false);
            } else if (tabToOpen.equalsIgnoreCase("Transfer")) {
                viewPager.setCurrentItem(2, false);
            } else {
                viewPager.setCurrentItem(0, false);
            }
        }
    }
}
