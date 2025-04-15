package com.moneymate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.moneymate.dashboard.DailyTransactionFragment;
import com.moneymate.dashboard.MonthlyTransactionFragment;

public class TransactionsPagerAdapter extends FragmentStateAdapter {

    public TransactionsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new DailyTransactionFragment() : new MonthlyTransactionFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
