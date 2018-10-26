package com.budget.lefevre.easybudget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mp3le on 3/28/2018.
 */

public class Budget {
    private String budgetName;
    private Double budgetAmount;
    private Double balanceLeft;
    private List<HistoryElement> history;

    // constructor for creation of budgets and for list display purposes
    Budget(String name, Double budgetAmount) {
        setBudgetName(name);
        setBudgetAmount(budgetAmount);
        setBalanceLeft(budgetAmount);
        history = new ArrayList<>();
        HistoryElement historyElement = new HistoryElement(HistoryType.BudgetCreate, getBudgetName(), budgetAmount);
        addToHistory(historyElement);
    }

    // constructor for setting all budget's variables
    Budget(String name, Double budgetAmount, Double balanceLeft, List<HistoryElement> history) {
        setBudgetName(name);
        setBudgetAmount(budgetAmount);
        setBalanceLeft(balanceLeft);
        setHistory(history);
    }

    // constructor for ....?
    Budget(String name, Double budgetAmount, List<HistoryElement> history) {
        setBudgetName(name);
        setBudgetAmount(budgetAmount);
        setHistory(history);
        setBalanceLeft(retrieveBalance());
    }

    // constructor for json/database purposes
    Budget() {}

    // setters
    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
    public void setBalanceLeft(Double balanceLeft) {
        this.balanceLeft = balanceLeft;
    }
    public void setHistory(List<HistoryElement> history) {
        this.history = history;
        setBalanceLeft(retrieveBalance());
    }
    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    // getters
    public List<HistoryElement> getHistory() {
        return history;
    }
    public Double getBalanceLeft() {
        return balanceLeft;
    }
    public Double getBudgetAmount() {
        return budgetAmount;
    }
    public String getBudgetName() {
        return budgetName;
    }

    // functions for budget functionality
    public Double retrieveBalance() {
        Double balance = 0.00;
        for (HistoryElement historyElement : history) {
            switch (historyElement.getTransactionType()) {
                case ManuallyEntered:
                    balance -= historyElement.getAmount();
                    break;
                case Withdrawal:
                    balance -= historyElement.getAmount();
                    break;
                case BudgetChanged:
                    balance += historyElement.getAmount();
                    break;
                case BudgetCreate:
                    balance += historyElement.getAmount();
                    break;
                case BudgetReset:
                    balance += historyElement.getAmount();
                    break;
                case Created:
                    balance += historyElement.getAmount();
                    break;
                case Transfer: {
                    balance -= historyElement.getAmount();
                    break;
                }
                default:
                    break;
            }
        }

        return balance;
    }

    public HistoryElement retrieveFirstElement() {
        if (getHistory().size() > 0) {
            return getHistory().get(0);
        }

        return null;
    }

    public void addToHistory(HistoryElement historyElement) {
        getHistory().add(0, historyElement);
        setBalanceLeft(retrieveBalance());
    }

    // for resetting budget
    public void resetBudget() {
        getHistory().clear();
        HistoryElement historyElement = new HistoryElement(HistoryType.BudgetReset, getBudgetName(), budgetAmount);
        addToHistory(historyElement);
        setBalanceLeft(retrieveBalance());
    }
}
