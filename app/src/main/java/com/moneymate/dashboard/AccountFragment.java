package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moneymate.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import com.moneymate.adapters.CashAdapter;
import com.moneymate.auth.Login;
import com.moneymate.models.CashModel;
import com.moneymate.adapters.InvestmentAdapter;
import com.moneymate.models.InvestmentModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountFragment extends Fragment {

    private static final String fetchCashAccountsURL = "http://10.0.2.2/moneymateBackend/fetchCashAccounts.php";
    private static final String fetchInvestmentAccountsURL = "http://10.0.2.2/moneymateBackend/fetchInvestmentAccounts.php";
    private static final String fetchNetworthURL = "http://10.0.2.2/moneymateBackend/fetchNetworth.php";
    private static final String deleteUserAccountURL = "http://10.0.2.2/moneymateBackend/deleteUser.php";

    private RecyclerView cashRecyclerView, investmentRecyclerView;
    private CashAdapter cashAdapter;
    private InvestmentAdapter investmentAdapter;
    private List<CashModel> cashAccountList;
    private List<InvestmentModel> investmentAccountList;
    private CardView cardUserProfile, cardCategories, cardSignOut, cardDeleteAccount;
    private ImageView createAccountBtn;
    private String userID;

    private static final Map<String, Integer> logoMap = new HashMap<>();
    static {
        logoMap.put("2131230854", R.drawable.bank_logo);
        logoMap.put("2131230869", R.drawable.cash_logo);
        logoMap.put("2131231011", R.drawable.wallet_logo);
        logoMap.put("2131231002", R.drawable.savings_logo);
        logoMap.put("2131230999", R.drawable.retirement_logo);
        logoMap.put("2131230916", R.drawable.investment_logo);
        logoMap.put("2131230856", R.drawable.bitcoin_logo);
    }

    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", " ");

        cashRecyclerView = view.findViewById(R.id.cashRecyclerView);
        cashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cashAccountList = new ArrayList<>();
        cashAdapter = new CashAdapter(requireContext(), cashAccountList);
        cashRecyclerView.setAdapter(cashAdapter);

        investmentRecyclerView = view.findViewById(R.id.investmentRecyclerView);
        investmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        investmentAccountList = new ArrayList<>();
        investmentAdapter = new InvestmentAdapter(requireContext(), investmentAccountList);
        investmentRecyclerView.setAdapter(investmentAdapter);

        Executor executor = Executors.newFixedThreadPool(3);
        executor.execute(() -> fetchCashAccounts(userID));
        executor.execute(() -> fetchInvestmentAccounts(userID));
        executor.execute(() -> setFetchNetworth(userID));

        createAccountBtn = view.findViewById(R.id.addAccountBtn);
        createAccountBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), SelectAccountType.class)));

        cardUserProfile = view.findViewById(R.id.cardUserProfile);
        cardUserProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), UserDetails.class)));

        cardCategories = view.findViewById(R.id.cardCategories);
        cardCategories.setOnClickListener(v -> startActivity(new Intent(getActivity(), CategoryViewPager.class)));

        cardSignOut = view.findViewById(R.id.cardSignOut);
        cardSignOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        cardDeleteAccount = view.findViewById(R.id.cardDeleteAccount);
        cardDeleteAccount.setOnClickListener(v -> showDeleteUserAccountDialog(userID));
    }

    private void setFetchNetworth(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchNetworthURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            String networth = jsonResponse.getString("networth");
                            requireActivity().runOnUiThread(() -> {
                                TextView networthTextView = requireView().findViewById(R.id.networthText);
                                networthTextView.setText(formatBalance(networth));
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void fetchCashAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchCashAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");
                        List<CashModel> tempList = new ArrayList<>();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            int logoResID = logoMap.getOrDefault(accountObject.getString("account_logo"), R.drawable.logo);
                            tempList.add(new CashModel(
                                    accountObject.getString("accountID"),
                                    accountObject.getString("account_type"),
                                    logoResID,
                                    accountObject.getString("account_name"),
                                    accountObject.getString("account_number"),
                                    accountObject.getString("currency"),
                                    accountObject.getString("balance")
                            ));
                        }

                        requireActivity().runOnUiThread(() -> {
                            cashAccountList.clear();
                            cashAccountList.addAll(tempList);
                            cashAdapter.notifyDataSetChanged();
                            updateCashUI();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void fetchInvestmentAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchInvestmentAccountsURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");
                        List<InvestmentModel> tempList = new ArrayList<>();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            int logoResID = logoMap.getOrDefault(accountObject.getString("account_logo"), R.drawable.logo);
                            tempList.add(new InvestmentModel(
                                    accountObject.getString("accountID"),
                                    accountObject.getString("account_type"),
                                    logoResID,
                                    accountObject.getString("account_name"),
                                    accountObject.getString("account_number"),
                                    accountObject.getString("currency"),
                                    accountObject.getString("balance")
                            ));
                        }

                        requireActivity().runOnUiThread(() -> {
                            investmentAccountList.clear();
                            investmentAccountList.addAll(tempList);
                            investmentAdapter.notifyDataSetChanged();
                            updateInvestmentUI();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void updateCashUI() {
        TextView noCashText = requireView().findViewById(R.id.noCashAccountsText);
        noCashText.setVisibility(cashAccountList.isEmpty() ? View.VISIBLE : View.GONE);
        cashRecyclerView.setVisibility(cashAccountList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateInvestmentUI() {
        TextView noInvestmentText = requireView().findViewById(R.id.noInvestmentAccountsText);
        noInvestmentText.setVisibility(investmentAccountList.isEmpty() ? View.VISIBLE : View.GONE);
        investmentRecyclerView.setVisibility(investmentAccountList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteUserAccountDialog(String userID) {
        new AlertDialog.Builder(requireContext())
                .setTitle("User Account Deletion")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteUserAccount(userID))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserAccount(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.POST, deleteUserAccountURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
                            sharedPreferences.edit().clear().apply();
                            Intent intent = new Intent(requireActivity(), Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                    }
                },
                error -> Log.e("VolleyError", "Request error: " + error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        Intent intent = new Intent(requireActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatBalance(String balance) {
        try {
            double amount = Double.parseDouble(balance);
            return String.format("₱ %,.2f", amount);
        } catch (NumberFormatException e) {
            return balance;
        }
    }
}
