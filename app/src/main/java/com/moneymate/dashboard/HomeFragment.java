package com.moneymate.dashboard;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.moneymate.R;
import com.moneymate.adapters.BudgetLimitedAdapter;
import com.moneymate.adapters.GoalLimitedAdapter;
import com.moneymate.models.BudgetModel;
import com.moneymate.models.GoalModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2/moneymateBackend/";
    private static final String fetchNetworthURL = BASE_URL + "fetchNetworth.php";
    private static final String fetchAllBudgets = BASE_URL + "fetchBudgetsLimited.php";
    private static final String fetchOngoingGoalsURL = BASE_URL + "fetchLimitedOngoingGoals.php";
    private static final String fetchIncomeExpenseByCategoryURL = BASE_URL + "fetchIncomeExpenseByCategory.php";

    private RecyclerView budgetsRecyclerView, onGoingGoalsRV;
    private final List<GoalModel> ongoingGoalsList = new ArrayList<>();
    private final List<BudgetModel> budgetsList = new ArrayList<>();
    private GoalLimitedAdapter ongoingGoalAdapter;
    private BudgetLimitedAdapter budgetAdapter;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        requestQueue = Volley.newRequestQueue(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);

        String userID = sharedPreferences.getString("userID", " ");
        String nickname = sharedPreferences.getString("nickname", "User");

        ((TextView) view.findViewById(R.id.greetings)).setText(getGreetingMessage() + ", " + nickname + "!");

        setupRecyclerViews(view);
        setupNavigationShortcuts(view);

        fetchNetworth(userID, view);
        fetchIncomeAndExpense(userID, view);
        fetchBudgets(userID, view);
        fetchOngoingGoals(userID, view);

        return view;
    }
    private void setupRecyclerViews(View view) {
        budgetsRecyclerView = view.findViewById(R.id.budgetsRV);
        budgetAdapter = new BudgetLimitedAdapter(requireContext(), budgetsList);
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        budgetsRecyclerView.setAdapter(budgetAdapter);

        onGoingGoalsRV = view.findViewById(R.id.onGoingGoalsRV);
        ongoingGoalAdapter = new GoalLimitedAdapter(getContext(), ongoingGoalsList);
        onGoingGoalsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        onGoingGoalsRV.setAdapter(ongoingGoalAdapter);
    }
    private void setupNavigationShortcuts(View view) {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);

        setupShortcut(view.findViewById(R.id.goAccounts), new AccountFragment(), R.id.menuAccount, bottomNav);
        setupShortcut(view.findViewById(R.id.goExpenses), new TransactionFragment(), R.id.menuTransactions, bottomNav);
        setupShortcut(view.findViewById(R.id.goIncome), new TransactionFragment(), R.id.menuTransactions, bottomNav);
        setupShortcut(view.findViewById(R.id.goBudget), new BudgetFragment(), R.id.menuBudget, bottomNav);
        setupShortcut(view.findViewById(R.id.goGoals), new BudgetFragment(), R.id.menuBudget, bottomNav);
    }
    private void setupShortcut(View view, Fragment fragment, int menuItemId, BottomNavigationView nav) {
        view.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            nav.setSelectedItemId(menuItemId);
        });
    }
    private String getGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good Morning";
        else if (hour < 18) return "Good Afternoon";
        else return "Good Evening";
    }
    private void fetchNetworth(String userID, View view) {
        StringRequest request = new StringRequest(Request.Method.POST, fetchNetworthURL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            ((TextView) view.findViewById(R.id.networthText)).setText(formatBalance(json.getString("networth")));
                            ((TextView) view.findViewById(R.id.accountsText)).setText(
                                    json.getString("account_count").equals("0") ? "No account" : json.getString("account_count") + " Accounts"
                            );
                        } else {
                            toast("Failed to fetch net worth");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Error parsing net worth");
                    }
                },
                error -> toast("Network Error")) {
            @Override protected Map<String, String> getParams() {
                return singletonParam("userID", userID);
            }
        };

        requestQueue.add(request);
    }
    private void fetchIncomeAndExpense(String userID, View view) {
        StringRequest request = new StringRequest(Request.Method.POST, fetchIncomeExpenseByCategoryURL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            ArrayList<PieEntry> incomeEntries = parsePieData(json.getJSONArray("incomeData"));
                            ArrayList<PieEntry> expenseEntries = parsePieData(json.getJSONArray("expenseData"));
                            setupDynamicPieChart(view.findViewById(R.id.donut_chart), "Incomes", incomeEntries);
                            setupDynamicPieChart(view.findViewById(R.id.donut_chart_expenses), "Expenses", expenseEntries);
                        } else toast("No transaction data yet");
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Parsing error");
                    }
                },
                error -> toast("Connection error")) {
            @Override protected Map<String, String> getParams() {
                return singletonParam("userID", userID);
            }
        };

        requestQueue.add(request);
    }
    private ArrayList<PieEntry> parsePieData(JSONArray array) throws Exception {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            entries.add(new PieEntry((float) obj.getDouble("total"), obj.optString("category", "Uncategorized")));
        }
        return entries;
    }
    private void setupDynamicPieChart(PieChart chart, String text, List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(generateColors(entries.size()));
        dataSet.setSliceSpace(2f);
        dataSet.setDrawValues(false);

        chart.setData(new PieData(dataSet));
        chart.setUsePercentValues(true);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(60f);
        chart.setTransparentCircleColor(Color.parseColor("#1AFFFFFF"));
        chart.setCenterText(text);
        chart.setCenterTextSize(20f);
        chart.setCenterTextColor(Color.WHITE);
        chart.setDrawEntryLabels(false);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(10.5f);
        legend.setTextColor(Color.BLACK);

        chart.animateY(1500);
        chart.invalidate();
    }
    private List<Integer> generateColors(int count) {
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float[] hsv = new float[]{(float) (Math.random() * 360), 0.5f + (float) (Math.random() * 0.5), 0.7f + (float) (Math.random() * 0.3)};
            colors.add(Color.HSVToColor(hsv));
        }
        return colors;
    }
    private void fetchBudgets(String userID, View view) {
        StringRequest request = new StringRequest(Request.Method.POST, fetchAllBudgets,
                response -> {
                    try {
                        JSONArray array = new JSONObject(response).getJSONArray("budgets");
                        budgetsList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            budgetsList.add(new BudgetModel(
                                    o.getString("budgetID"),
                                    o.getString("budget_name"),
                                    o.getString("category"),
                                    o.getDouble("amount"),
                                    o.optDouble("total_spent", 0)
                            ));
                        }

                        budgetAdapter.notifyDataSetChanged();
                        toggleVisibility(view.findViewById(R.id.noBudgetText), view.findViewById(R.id.budgetsRV), budgetsList.isEmpty());

                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Failed to load budgets");
                    }
                },
                error -> toast("Network error loading budgets")) {
            @Override protected Map<String, String> getParams() {
                return singletonParam("userID", userID);
            }
        };

        requestQueue.add(request);
    }
    private void fetchOngoingGoals(String userID, View view) {
        StringRequest request = new StringRequest(Request.Method.POST, fetchOngoingGoalsURL,
                response -> {
                    try {
                        JSONArray array = new JSONObject(response).getJSONArray("goals");
                        ongoingGoalsList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            ongoingGoalsList.add(new GoalModel(
                                    o.getString("goalID"),
                                    o.getString("goal_title"),
                                    o.getString("target_date"),
                                    o.getDouble("amount"),
                                    o.optDouble("account_balance", 0),
                                    o.getString("goal_completion")
                            ));
                        }

                        ongoingGoalAdapter.notifyDataSetChanged();
                        toggleVisibility(view.findViewById(R.id.noOnGoingGoal), view.findViewById(R.id.onGoingGoalsRV), ongoingGoalsList.isEmpty());

                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Failed to load goals");
                    }
                },
                error -> toast("Network Error")) {
            @Override protected Map<String, String> getParams() {
                return singletonParam("userID", userID);
            }
        };
        requestQueue.add(request);
    }
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void toggleVisibility(View emptyView, View listView, boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    private String formatBalance(String balance) {
        try {
            return String.format("₱ %,.2f", Double.parseDouble(balance));
        } catch (NumberFormatException e) {
            return balance;
        }
    }
    private Map<String, String> singletonParam(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}