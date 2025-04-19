package com.moneymate.models;

public class GoalModel {
    private String goalID, goalName, goalDate, goalCompletion;
    private Double goalAmount, accountAmount;

    public GoalModel(String goalID, String goalName, String goalDate, Double goalAmount, Double accountAmount, String goalCompletion) {
        this.goalID = goalID;
        this.goalName = goalName;
        this.goalDate = goalDate;
        this.goalAmount = goalAmount;
        this.accountAmount = accountAmount;
        this.goalCompletion = goalCompletion;
    }

    public String getGoalID() { return goalID; }
    public String getGoalName() {
        return goalName;
    }
    public String getGoalDate() {return goalDate; }
    public Double getGoalAmount() {
        return goalAmount;
    }
    public Double getAccountAmount() {
        return accountAmount;
    }
    public String getGoalCompletion() {
        return goalCompletion;
    }
}