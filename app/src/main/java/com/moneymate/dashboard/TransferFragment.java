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
import com.moneymate.adapters.TransactionsAdapter;
import com.moneymate.models.TransactionsModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferFragment extends Fragment {

    private static final String fetchAllTransactionsURL = "http://10.0.2.2/moneymateBackend/fetchAllTransfers.php";
    private RecyclerView transactionsRecyclerView;
    private List<TransactionsModel> transactionsList = new ArrayList<>();
    private TransactionsAdapter transactionsAdapter;
    private View rootView;

    public TransferFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_transfer, container, false);

        transactionsRecyclerView = rootView.findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionsAdapter = new TransactionsAdapter(getContext(), transactionsList);
        transactionsRecyclerView.setAdapter(transactionsAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", "1");

        fetchTransactions(userID);

        return rootView;
    }

    private void fetchTransactions(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        String accountID = "0";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchAllTransactionsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("transactions");
                        transactionsList.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String transactionID = accountObject.getString("transactionID");
                            String transaction_name = accountObject.getString("transaction_name");
                            String amount = accountObject.getString("amount");
                            String category = accountObject.getString("category");
                            String transactionType = accountObject.getString("transaction_type");
                            String transactionDate = accountObject.getString("transaction_date");

                            transactionsList.add(new TransactionsModel(transactionID, accountID, transaction_name, amount, category, transactionType, transactionDate));
                        }

                        transactionsAdapter.notifyDataSetChanged();

                        TextView noTransaction = rootView.findViewById(R.id.noTransactionText);
                        noTransaction.setVisibility(transactionsList.isEmpty() ? View.VISIBLE : View.GONE);
                        transactionsRecyclerView.setVisibility(transactionsList.isEmpty() ? View.GONE : View.VISIBLE);

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
                params.put("accountID", accountID);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}