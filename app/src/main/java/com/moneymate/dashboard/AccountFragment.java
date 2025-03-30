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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.moneymate.adapters.CashAdapter;
import com.moneymate.auth.Login;
import com.moneymate.auth.Register;
import com.moneymate.models.CashModel;
import com.moneymate.adapters.InvestmentAdapter;
import com.moneymate.models.InvestmentModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class AccountFragment extends Fragment {

    private static final String fetchCashAccounts = "http://192.168.1.6/moneymateBackend/fetchCashAccounts.php";
    private static final String fetchInvestmentAccounts = "http://192.168.1.6/moneymateBackend/fetchInvestmentAccounts.php";
    private static final String fetchNetworth = "http://192.168.1.6/moneymateBackend/fetchNetworth.php";

    private RecyclerView cashRecyclerView, investmentRecyclerView;
    private CashAdapter cashAdapter;
    private InvestmentAdapter investmentAdapter;
    private List<CashModel> cashAccountList;
    private List<InvestmentModel> investmentAccountList;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve user data
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        String userID = sharedPreferences.getString("userID", " ");

        // Initialize Cash Accounts RecyclerView
        cashRecyclerView = view.findViewById(R.id.cashRecyclerView);
        cashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cashAccountList = new ArrayList<>();
        cashAdapter = new CashAdapter(cashAccountList);
        cashRecyclerView.setAdapter(cashAdapter);

        // Initialize Investment Accounts RecyclerView
        investmentRecyclerView = view.findViewById(R.id.investmentRecyclerView);
        investmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        investmentAccountList = new ArrayList<>();
        investmentAdapter = new InvestmentAdapter(investmentAccountList);
        investmentRecyclerView.setAdapter(investmentAdapter);

        // For Create New Account
        ImageView createAccountBtn = view.findViewById(R.id.addAccountBtn);
        createAccountBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SelectAccountType.class);
                startActivity(intent);
            }
        });

        CardView cardUserProfile = view.findViewById(R.id.cardUserProfile);
        cardUserProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), UserDetails.class);
                startActivity(intent);
            }
        });

        CardView cardCategories = view.findViewById(R.id.cardCategories);
        cardCategories.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CategoryViewPager.class);
                startActivity(intent);
            }
        });

        // Logout confirmation before signing out
        CardView cardSignOut = view.findViewById(R.id.cardSignOut);
        cardSignOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Delay Fetching Account Datas
        new android.os.Handler().postDelayed(() -> {
            fetchCashAccounts(userID);
            fetchInvestmentAccounts(userID);
            setFetchNetworth(userID);
        }, 200);
    }

    private void setFetchNetworth(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchNetworth,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            // Directly get the networth value (it's not an array)
                            String networth = jsonResponse.getString("networth");

                            // Update UI with net worth
                            TextView networthTextView = requireView().findViewById(R.id.networthText);
                            networthTextView.setText("₱ " + networth);
                        } else {
                            Toast.makeText(requireContext(), "Failed to fetch net worth", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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
    private void fetchCashAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchCashAccounts,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");
                        cashAccountList.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String accountID = accountObject.getString("accountID");
                            String account_type = accountObject.getString("account_type");
                            String accountLogoString = accountObject.getString("account_logo");
                            String account_name = accountObject.getString("account_name");
                            String account_number = accountObject.getString("account_number");
                            String currency = accountObject.getString("currency");
                            String balance = accountObject.getString("balance");

                            // Convert the string logo ID to an integer resource ID
                            int accountLogoResID = getResources().getIdentifier(accountLogoString, "drawable", requireContext().getPackageName());

                            cashAccountList.add(new CashModel(accountID, account_type, accountLogoResID, account_name, account_number, currency, balance));
                        }
                        cashAdapter.notifyDataSetChanged();

                        // Show/hide "No accounts yet" message
                        TextView noCashAccountsText = requireView().findViewById(R.id.noCashAccountsText);
                        noCashAccountsText.setVisibility(cashAccountList.isEmpty() ? View.VISIBLE : View.GONE);
                        cashRecyclerView.setVisibility(cashAccountList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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

    private void fetchInvestmentAccounts(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchInvestmentAccounts,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray accountsArray = jsonResponse.getJSONArray("accounts");
                        investmentAccountList.clear();

                        for (int i = 0; i < accountsArray.length(); i++) {
                            JSONObject accountObject = accountsArray.getJSONObject(i);
                            String accountID = accountObject.getString("accountID");
                            String account_type = accountObject.getString("account_type");
                            String accountLogoString = accountObject.getString("account_logo");
                            String account_name = accountObject.getString("account_name");
                            String account_number = accountObject.getString("account_number");
                            String currency = accountObject.getString("currency");
                            String balance = accountObject.getString("balance");

                            // Convert the string logo ID to an integer resource ID
                            int accountLogoResID = getResources().getIdentifier(accountLogoString, "drawable", requireContext().getPackageName());

                            investmentAccountList.add(new InvestmentModel(accountID, account_type, accountLogoResID, account_name, account_number, currency, balance));
                        }

                        investmentAdapter.notifyDataSetChanged();

                        // Show/hide "No accounts yet" message
                        TextView noInvestmentAccountsText = requireView().findViewById(R.id.noInvestmentAccountsText);
                        noInvestmentAccountsText.setVisibility(investmentAccountList.isEmpty() ? View.VISIBLE : View.GONE);
                        investmentRecyclerView.setVisibility(investmentAccountList.isEmpty() ? View.GONE : View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to load accounts. Try again!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Network Error. Check your connection!", Toast.LENGTH_SHORT).show();
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

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {
        if (getActivity() != null) {
            // Clear saved user data
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Navigate to Login Page and clear activity stack
            Intent intent = new Intent(requireActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
