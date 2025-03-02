package com.moneymate.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.moneymate.R;
import com.moneymate.adapters.GoalAdapter;
import com.moneymate.models.Goal;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ProgressBar and TextView
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView progressText = view.findViewById(R.id.progressText);
        TextView progressPercentage = view.findViewById(R.id.progressPercentage);

        // Set progress value
        int progressValue = 120;
        int maxValue = 1000;
        progressBar.setMax(maxValue);
        progressBar.setProgress(progressValue);

        // Calculate percentage
        int percentage = (int) ((progressValue / (float) maxValue) * 100);

        // Set text overlay
        progressPercentage.setText(String.valueOf(percentage) + "%");
        progressText.setText("P " + progressValue + " of P " + maxValue);

        // Initialize and configure the first PieChart (Bills)
        PieChart billsChart = view.findViewById(R.id.donut_chart);
        setupPieChart(billsChart, "Bills", new float[]{20f, 20f, 20f, 20f, 20f},
                new String[]{"Expenses", "Income", "Expenses", "Income", "Expenses"},
                new String[]{"#9C27B0", "#FFC107", "#795548", "#607D8B", "#8BC34A"});

        // Initialize and configure the second PieChart (Expenses)
        PieChart expensesChart = view.findViewById(R.id.donut_chart_expenses);
        setupPieChart(expensesChart, "Expenses", new float[]{40f, 25f, 15f, 15f, 5f},
                new String[]{"Rent", "Food", "Entertainment", "Transport", "Others"},
                new String[]{"#FF4081", "#3F51B5", "#FFA000", "#4CAF50", "#00BCD4"});

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.goalsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data for goals
        List<Goal> goalList = new ArrayList<>();
        goalList.add(new Goal("Save for a vacation"));
        goalList.add(new Goal("Buy a new laptop"));
        goalList.add(new Goal("Pay off credit card"));
        goalList.add(new Goal("Emergency fund"));

        // Set up adapter
        GoalAdapter adapter = new GoalAdapter(goalList, goal -> {
            // Handle goal click
        });

        recyclerView.setAdapter(adapter);

        // Get reference to BottomNavigationView
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);

        // Find buttons
        ImageView goAccounts = view.findViewById(R.id.goAccounts);
        ImageView goBills = view.findViewById(R.id.goBills);
        ImageView goTransactions = view.findViewById(R.id.goExpenses);
        ImageView goBudget = view.findViewById(R.id.goBudget);
        ImageView goGoals = view.findViewById(R.id.goGoals);

        // Set click listeners for each button
        goAccounts.setOnClickListener(v -> navigateToFragment(new AccountFragment(), R.id.menuAccount, bottomNavigationView));
        goBills.setOnClickListener(v -> navigateToFragment(new BillsFragment(), R.id.menuBills, bottomNavigationView));
        goTransactions.setOnClickListener(v -> navigateToFragment(new TransactionFragment(), R.id.menuTransactions, bottomNavigationView));
        goBudget.setOnClickListener(v -> navigateToFragment(new BudgetFragment(), R.id.menuBudget, bottomNavigationView));
        goGoals.setOnClickListener(v -> navigateToFragment(new BudgetFragment(), R.id.menuBudget, bottomNavigationView));

        return view;
    }

    /**
     * Configures a PieChart with custom data, colors, and appearance settings.
     *
     * @param pieChart   The PieChart to be configured.
     * @param centerText The text displayed in the center of the PieChart.
     * @param values     The values for each PieChart slice.
     * @param labels     The labels for each slice.
     * @param colorsHex  The colors for each slice in hex format.
     */

//  For Charts
    private void setupPieChart(PieChart pieChart, String centerText, float[] values, String[] labels, String[] colorsHex) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entries.add(new PieEntry(values[i], labels[i]));
        }

        // Convert color hex codes to integer color values
        int[] colors = new int[colorsHex.length];
        for (int i = 0; i < colorsHex.length; i++) {
            colors[i] = Color.parseColor(colorsHex[i]);
        }

        // Configure PieDataSet
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(false);

        // Create PieData and set it to the PieChart
        PieData pieData = new PieData(dataSet);
        pieChart.setUsePercentValues(true);
        pieChart.setData(pieData);

        // Configure PieChart appearance
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setTransparentCircleColor(Color.parseColor("#1AFFFFFF"));
        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(20f);
        pieChart.setCenterTextColor(Color.parseColor("#FFFFFF"));

        // Hide entry labels and disable description
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);

        // Configure legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.BLACK);

        // Animate the chart
        pieChart.animateY(1500);
    }

    // Method to navigate to a fragment and update bottom navigation selection
    private void navigateToFragment(Fragment fragment, int menuItemId, BottomNavigationView bottomNavigationView) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null); // Allows back navigation
        transaction.commit();

        // Update Bottom Navigation Selection
        bottomNavigationView.setSelectedItemId(menuItemId);
    }
}
