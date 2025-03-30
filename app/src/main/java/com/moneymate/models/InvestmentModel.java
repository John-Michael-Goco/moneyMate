package com.moneymate.models;

public class InvestmentModel {
    private int accountLogo;
    private String accountID, accountType, accountName, accountNumber, currency, balance;

    public InvestmentModel(String accountID, String accountType, int accountLogo, String accountName, String accountNumber, String currency, String balance) {
        this.accountID = accountID;
        this.accountType = accountType;
        this.accountLogo = accountLogo;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
    }

    public String getAccountID() { return accountID;}
    public String getAccountType() {
        return accountType;
    }
    public int getAccountLogo() {
        return accountLogo;
    }
    public String getAccountName() {
        return accountName;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public String getCurrency() {
        return currency;
    }
    public String getBalance() {
        return balance;
    }
}
