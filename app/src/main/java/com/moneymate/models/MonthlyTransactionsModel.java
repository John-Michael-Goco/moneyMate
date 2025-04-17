package com.moneymate.models;

public class MonthlyTransactionsModel {

    private String monthTitle, transactionType, totalAmount;

    public MonthlyTransactionsModel(String monthTitle, String transactionType, String totalAmount) {
        this.monthTitle = monthTitle;
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
    }

    public String getMonthTitle() { return monthTitle; }
    public String getTransactionType() { return transactionType; }
    public String getTotalAmount() {
        return totalAmount;
    }
}
