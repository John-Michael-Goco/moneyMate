package com.moneymate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.moneymate.dashboard.BudgetTabFragment;
import com.moneymate.dashboard.GoalTabFragment;

public class GoalPagerAdapter extends FragmentStateAdapter {

    public GoalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new BudgetTabFragment() : new GoalTabFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
