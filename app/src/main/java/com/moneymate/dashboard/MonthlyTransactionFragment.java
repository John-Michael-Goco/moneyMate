package com.moneymate.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moneymate.R;
import com.moneymate.adapters.MonthlyTransactionsAdapter;
import com.moneymate.models.MonthlyTransactionsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyTransactionFragment extends Fragment {

    private static final String fetchMonthlyTransactionsURL = "http://10.0.2.2/moneymateBackend/fetchMonthlyTransactions.php";
    private RecyclerView monthlyTransactionRV;
    private List<MonthlyTransactionsModel> monthlyTransactionsList = new ArrayList<>();
    private MonthlyTransactionsAdapter monthlyTransactionsAdapter;
    private View rootView;

    public MonthlyTransactionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_monthly_transaction, container, false);

        monthlyTransactionRV = rootView.findViewById(R.id.monthlyTransactionRV);
        monthlyTransactionRV.setLayoutManager(new LinearLayoutManager(getContext()));
        monthlyTransactionsAdapter = new MonthlyTransactionsAdapter(getContext(), monthlyTransactionsList);
        monthlyTransactionRV.setAdapter(monthlyTransactionsAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        fetchMonthlyTransactions(userID);

        return rootView;
    }

    private void fetchMonthlyTransactions(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchMonthlyTransactionsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("monthlyTransactions");
                        monthlyTransactionsList.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String monthTitle = accountObject.getString("monthTitle");
                            String transaction_type = accountObject.getString("transaction_type");
                            String totalAmount = accountObject.getString("totalAmount");

                            monthlyTransactionsList.add(new MonthlyTransactionsModel(monthTitle, transaction_type, totalAmount));
                        }

                        monthlyTransactionsAdapter.notifyDataSetChanged();

                        TextView noTransaction = rootView.findViewById(R.id.noTransactionText);
                        noTransaction.setVisibility(monthlyTransactionsList.isEmpty() ? View.VISIBLE : View.GONE);
                        monthlyTransactionRV.setVisibility(monthlyTransactionsList.isEmpty() ? View.GONE : View.VISIBLE);

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
