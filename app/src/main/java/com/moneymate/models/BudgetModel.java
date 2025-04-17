package com.moneymate.models;

public class BudgetModel {

    private String budgetID, budgetName, budgetCategory;
    private Double budgetAmount, totalSpent;
    public BudgetModel(String budgetID, String budgetName, String budgetCategory, Double budgetAmount, Double totalSpent) {
        this.budgetID = budgetID;
        this.budgetName = budgetName;
        this.budgetCategory = budgetCategory;
        this.budgetAmount = budgetAmount;
        this.totalSpent = totalSpent;
    }

    public String getBudgetID() { return budgetID;}
    public String getBudgetName() {
        return budgetName;
    }
    public String getBudgetCategory() {
        return budgetCategory;
    }
    public Double getBudgetAmount() {
        return budgetAmount;
    }
    public Double getTotalSpent() {
        return totalSpent;
    }
}
