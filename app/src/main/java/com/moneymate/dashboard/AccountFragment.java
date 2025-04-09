package com.moneymate.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
        userID = sharedPreferences.getString("userID", " ");

        // Initialize Cash Accounts RecyclerView
        cashRecyclerView = view.findViewById(R.id.cashRecyclerView);
        cashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cashAccountList = new ArrayList<>();
        cashAdapter = new CashAdapter(requireContext(), cashAccountList);
        cashRecyclerView.setAdapter(cashAdapter);

        // Initialize Investment Accounts RecyclerView
        investmentRecyclerView = view.findViewById(R.id.investmentRecyclerView);
        investmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        investmentAccountList = new ArrayList<>();
        investmentAdapter = new InvestmentAdapter(requireContext(), investmentAccountList);
        investmentRecyclerView.setAdapter(investmentAdapter);

        // For Create New Account
        createAccountBtn = view.findViewById(R.id.addAccountBtn);
        createAccountBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SelectAccountType.class);
                startActivity(intent);
            }
        });

        cardUserProfile = view.findViewById(R.id.cardUserProfile);
        cardUserProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), UserDetails.class);
                startActivity(intent);
            }
        });

        cardCategories = view.findViewById(R.id.cardCategories);
        cardCategories.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), CategoryViewPager.class);
                startActivity(intent);
            }
        });

        // Logout confirmation before signing out
        cardSignOut = view.findViewById(R.id.cardSignOut);
        cardSignOut.setOnClickListener(v -> showLogoutConfirmationDialog());

        // User account deletion confirmation
        cardDeleteAccount = view.findViewById(R.id.cardDeleteAccount);
        cardDeleteAccount.setOnClickListener(v -> showDeleteUserAccountDialog(userID));

        // Delay Fetching Account Datas
        if (!userID.isEmpty()) {
            new android.os.Handler().postDelayed(() -> {
                fetchCashAccounts(userID);
                fetchInvestmentAccounts(userID);
                setFetchNetworth(userID);
            }, 250);
        } else {
            Toast.makeText(requireContext(), "User ID is invalid!", Toast.LENGTH_SHORT).show();
        }
    }
    private void setFetchNetworth(String userID) {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchNetworthURL,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getString("status").equals("success")) {
                            // Directly get the networth value (it's not an array)
                            String networth = jsonResponse.getString("networth");

                            // Update UI with net worth
                            TextView networthTextView = requireView().findViewById(R.id.networthText);
                            networthTextView.setText(formatBalance(networth));
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchCashAccountsURL,
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

        StringRequest stringRequest = new StringRequest(Request.Method.POST, fetchInvestmentAccountsURL,
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
                            Toast.makeText(requireContext(), "User account deleted successfully!", Toast.LENGTH_SHORT).show();

                            // Clear user data
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();

                            // Navigate to login page
                            Intent intent = new Intent(requireActivity(), Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();

                        } else {
                            Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Parsing error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error parsing response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Request error: " + error.toString());
                    Toast.makeText(requireContext(), "Error deleting user account.", Toast.LENGTH_SHORT).show();
                }) {
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
    private String formatBalance(String balance) {
        try {
            double amount = Double.parseDouble(balance);
            return String.format("₱ %,.2f", amount);
        } catch (NumberFormatException e) {
            return balance;
        }
    }
}
