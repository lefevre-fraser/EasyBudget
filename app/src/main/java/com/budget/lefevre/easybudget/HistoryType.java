package com.budget.lefevre.easybudget;

// an enum for determining the type of transaction made
public enum HistoryType {
    Deposit,
    Withdrawal,
    Transfer,
    Created,
    Reset,
    BudgetReset,
    BudgetCreate,
    BudgetChanged,
    ManuallyEntered
}
