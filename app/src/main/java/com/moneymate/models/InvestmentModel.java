package com.moneymate.models;

public class InvestmentModel {
    private int logoResId;
    private String title;
    private String amount;

    public InvestmentModel(int logoResId, String title, String amount) {
        this.logoResId = logoResId;
        this.title = title;
        this.amount = amount;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public String getTitle() {
        return title;
    }
    public String getAmount() {
        return amount;
    }
}
