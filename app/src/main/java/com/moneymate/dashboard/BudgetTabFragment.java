package com.moneymate.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moneymate.R;
import com.moneymate.adapters.BudgetAdapter;
import com.moneymate.adapters.TransactionsAdapter;
import com.moneymate.models.BudgetModel;
import com.moneymate.models.TransactionsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetTabFragment extends Fragment {
    private static final String fetchAllBudgets = "http://10.0.2.2/moneymateBackend/fetchAllBudgets.php";
    private ImageView addBtn;
    private LinearLayout headerLayout;
    private RecyclerView budgetsRecyclerView;
    private List<BudgetModel> budgetsList = new ArrayList<>();
    private BudgetAdapter budgetAdapter;
    private View rootView;

    public BudgetTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_budget_tab, container, false);

        headerLayout = rootView.findViewById(R.id.headerLayout);

        budgetsRecyclerView = rootView.findViewById(R.id.budgetsRV);
        budgetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        budgetAdapter = new BudgetAdapter(getContext(), budgetsList);
        budgetsRecyclerView.setAdapter(budgetAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        addBtn = rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CreateBudget.class);
                startActivity(intent);
            }
        });

        fetchBudgets(userID);

        return rootView;
    }
    private void fetchBudgets(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAllBudgets,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray budgetsArray = jsonResponse.getJSONArray("budgets");
                        budgetsList.clear();

                        for (int i = 0; i < budgetsArray.length(); i++) {
                            JSONObject budgetObject = budgetsArray.getJSONObject(i);
                            String budgetID = budgetObject.getString("budgetID");
                            String budget_name = budgetObject.getString("budget_name");
                            String category = budgetObject.getString("category");
                            double amount = Double.parseDouble(budgetObject.getString("amount"));
                            double total_spent = Double.parseDouble(budgetObject.optString("total_spent", "0"));

                            budgetsList.add(new BudgetModel(budgetID, budget_name, category, amount, total_spent));
                        }

                        budgetAdapter.notifyDataSetChanged();

                        TextView noBudget = rootView.findViewById(R.id.noBudgetText);
                        noBudget.setVisibility(budgetsList.isEmpty() ? View.VISIBLE : View.GONE);
                        budgetsRecyclerView.setVisibility(budgetsList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load transactions. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getActivity(), "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
