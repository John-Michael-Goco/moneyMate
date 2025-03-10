package com.moneymate.dashboard;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.moneymate.R;
import java.util.ArrayList;
import java.util.List;

import com.moneymate.adapters.CashAdapter;
import com.moneymate.auth.ForgotPasswordCode;
import com.moneymate.auth.ForgotPasswordEmail;
import com.moneymate.models.CashModel;
import com.moneymate.adapters.CreditAdapter;
import com.moneymate.models.CreditModel;
import com.moneymate.adapters.InvestmentAdapter;
import com.moneymate.models.InvestmentModel;

public class AccountFragment extends Fragment {

    private RecyclerView cashRecyclerView, creditRecyclerView, investmentRecyclerView;
    private CashAdapter cashAdapter;

    // private CreditAdapter creditAdapter;
    private InvestmentAdapter investmentAdapter;
    private List<CashModel> cashAccountList;

    // private List<CreditModel> creditAccountList;
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

        // Initialize Cash Accounts RecyclerView
        cashRecyclerView = view.findViewById(R.id.cashRecyclerView);
        cashRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cashAccountList = new ArrayList<>();
        loadCashSampleData();

        cashAdapter = new CashAdapter(cashAccountList);
        cashRecyclerView.setAdapter(cashAdapter);

        // Initialize Credit Accounts RecyclerView
        // creditRecyclerView = view.findViewById(R.id.creditRecyclerView);
        // creditRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // creditAccountList = new ArrayList<>();
        // loadCreditSampleData();

        // creditAdapter = new CreditAdapter(creditAccountList);
        // creditRecyclerView.setAdapter(creditAdapter);

        // Initialize Investment Accounts RecyclerView
        investmentRecyclerView = view.findViewById(R.id.investmentRecyclerView);
        investmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        investmentAccountList = new ArrayList<>();
        loadInvestmentSampleData();

        investmentAdapter = new InvestmentAdapter(investmentAccountList);
        investmentRecyclerView.setAdapter(investmentAdapter);

        ImageView createAccountBtn = view.findViewById(R.id.addAccountBtn);
        if (createAccountBtn != null) {
            createAccountBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SelectAccountType.class);
                startActivity(intent);
            });
        }
    }

    private void loadCashSampleData() {
        cashAccountList.add(new CashModel(R.drawable.logo, "Savings Account", "₱10,000"));
        cashAccountList.add(new CashModel(R.drawable.logo, "Checking Account", "₱5,500"));
        cashAccountList.add(new CashModel(R.drawable.logo, "Cash on Hand", "₱2,000"));
    }

//    private void loadCreditSampleData() {
//        creditAccountList.add(new CreditModel(R.drawable.logo, "Credit Card 1", "₱-12,000"));
//        creditAccountList.add(new CreditModel(R.drawable.logo, "Credit Card 2", "₱-8,500"));
//        creditAccountList.add(new CreditModel(R.drawable.logo, "Personal Loan", "₱-25,000"));
//    }

    private void loadInvestmentSampleData() {
        investmentAccountList.add(new InvestmentModel(R.drawable.logo, "Stocks", "₱50,000"));
        investmentAccountList.add(new InvestmentModel(R.drawable.logo, "Mutual Funds", "₱30,000"));
        investmentAccountList.add(new InvestmentModel(R.drawable.logo, "Real Estate", "₱150,000"));
    }
}
