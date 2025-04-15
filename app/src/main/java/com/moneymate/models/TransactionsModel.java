package com.moneymate.models;

public class TransactionsModel {

    private String transactionID, accountID, transactionName, amount, category, categoryType, transactionDate;

    public TransactionsModel(String transactionID, String accountID, String transactionName, String amount, String category, String categoryType, String transactionDate) {
        this.transactionID = transactionID;
        this.accountID = accountID;
        this.transactionName = transactionName;
        this.amount = amount;
        this.category = category;
        this.categoryType = categoryType;
        this.transactionDate = transactionDate;
    }

    public String getTransactionID() { return transactionID; }
    public String getAccountID() { return accountID; }
    public String getTransactionName() {
        return transactionName;
    }
    public String getAmount() {return amount; }
    public String getCategory() {return category; }
    public String getCategoryType() { return categoryType; }
    public String getTransactionDate() {return transactionDate; }
}
