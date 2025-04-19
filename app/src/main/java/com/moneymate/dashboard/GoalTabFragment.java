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
import com.moneymate.adapters.GoalAdapter;
import com.moneymate.models.BudgetModel;
import com.moneymate.models.GoalModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalTabFragment extends Fragment {

    private static final String fetchOngoingGoalsURL = "http://10.0.2.2/moneymateBackend/fetchOngoingGoals.php";
    private static final String fetchCompletedGoalURL = "http://10.0.2.2/moneymateBackend/fetchCompletedGoals.php";
    private ImageView addBtn;
    private TextView noOnGoingGoal, noCompletedGoal;
    private LinearLayout headerLayout;
    private RecyclerView onGoingGoals, completedGoals;
    private List<GoalModel> ongoingGoalsList = new ArrayList<>();
    private List<GoalModel> completedGoalsList = new ArrayList<>();
    private GoalAdapter ongoingGoalAdapter;
    private GoalAdapter completedGoalAdapter;
    private View rootView;

    public GoalTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_goal_tab, container, false);

        headerLayout = rootView.findViewById(R.id.headerLayout);

        onGoingGoals = rootView.findViewById(R.id.onGoingGoals);
        onGoingGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        ongoingGoalAdapter = new GoalAdapter(getContext(), ongoingGoalsList);
        onGoingGoals.setAdapter(ongoingGoalAdapter);

        completedGoals = rootView.findViewById(R.id.completedGoals);
        completedGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        completedGoalAdapter = new GoalAdapter(getContext(), completedGoalsList);
        completedGoals.setAdapter(completedGoalAdapter);

        // Fetch userID
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        // Handle Add Button
        addBtn = rootView.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CreateGoal.class);
                startActivity(intent);
            }
        });

        fetchOngoingGoals(userID);
        fetchCompletedGoals(userID);

        return rootView;
    }
    private void fetchOngoingGoals(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchOngoingGoalsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray goalsArray = jsonResponse.getJSONArray("goals");
                        ongoingGoalsList.clear();

                        for (int i = 0; i < goalsArray.length(); i++) {
                            JSONObject goalObject = goalsArray.getJSONObject(i);
                            String goalID = goalObject.getString("goalID");
                            String goalName = goalObject.getString("goal_title");
                            String goalDate = goalObject.getString("target_date");
                            String goal_completion = goalObject.getString("goal_completion");
                            double goalAmount = Double.parseDouble(goalObject.getString("amount"));
                            double accountBalance = Double.parseDouble(goalObject.optString("account_balance", "0"));

                            ongoingGoalsList.add(new GoalModel(goalID, goalName, goalDate, goalAmount, accountBalance, goal_completion));
                        }

                        ongoingGoalAdapter.notifyDataSetChanged();

                        noOnGoingGoal = rootView.findViewById(R.id.noOnGoingGoal);
                        noOnGoingGoal.setVisibility(ongoingGoalsList.isEmpty() ? View.VISIBLE : View.GONE);
                        onGoingGoals.setVisibility(ongoingGoalsList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load goals. Try again!", Toast.LENGTH_SHORT).show();
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
    private void fetchCompletedGoals(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchCompletedGoalURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray goalsArray = jsonResponse.getJSONArray("goals");
                        completedGoalsList.clear();

                        for (int i = 0; i < goalsArray.length(); i++) {
                            JSONObject goalObject = goalsArray.getJSONObject(i);
                            String goalID = goalObject.getString("goalID");
                            String goalName = goalObject.getString("goal_title");
                            String goalDate = goalObject.getString("target_date");
                            String goal_completion = goalObject.getString("goal_completion");
                            double goalAmount = Double.parseDouble(goalObject.getString("amount"));
                            double accountBalance = Double.parseDouble(goalObject.optString("account_balance", "0"));

                            completedGoalsList.add(new GoalModel(goalID, goalName, goalDate, goalAmount, accountBalance, goal_completion));
                        }

                        completedGoalAdapter.notifyDataSetChanged();

                        noCompletedGoal = rootView.findViewById(R.id.noCompletedGoal);
                        noCompletedGoal.setVisibility(completedGoalsList.isEmpty() ? View.VISIBLE : View.GONE);
                        completedGoals.setVisibility(completedGoalsList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load goals. Try again!", Toast.LENGTH_SHORT).show();
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