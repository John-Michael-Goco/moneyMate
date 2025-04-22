package com.moneymate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.moneymate.dashboard.ExpensesFragment;
import com.moneymate.dashboard.IncomesFragment;
import com.moneymate.dashboard.TransferFragment;
import com.moneymate.dashboard.MonthlyTransactionFragment;

public class TransactionsPagerAdapter extends FragmentStateAdapter {

    public TransactionsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ExpensesFragment();
            case 1:
                return new IncomesFragment();
            case 2:
                return new TransferFragment();
            case 3:
                return new MonthlyTransactionFragment();
            default:
                return new ExpensesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // For 4 tabs
    }
}
