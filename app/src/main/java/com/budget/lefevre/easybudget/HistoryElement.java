package com.budget.lefevre.easybudget;

import java.util.Locale;

public class HistoryElement {
    private HistoryType transactionType;
    private String reason;
    private String associatedAccount1;
    private String associatedAccount2;
    private String associatedBudget;
    private Double amount;

    // for transfers that go on a budget
    HistoryElement(HistoryType transactionType, String associatedAccount1, String associatedAccount2, String associatedBudget, Double amount) {
        setTransactionType(transactionType);
        setAssociatedAccount1(associatedAccount1);
        setAssociatedAccount2(associatedAccount2);
        setAssociatedBudget(associatedBudget);
        setAmount(amount);
    }

    // for regular transfers
    HistoryElement(HistoryType transactionType, String associatedAccount1, String associatedAccount2, Double amount) {
        setTransactionType(transactionType);
        setAssociatedAccount1(associatedAccount1);
        setAssociatedAccount2(associatedAccount2);
        setAmount(amount);
    }

    // for withdrawals that are added to budgets
    HistoryElement(HistoryType transactionType, String associatedAccount1, String associatedBudget, Double amount, String reason) {
        setTransactionType(transactionType);
        setAssociatedAccount1(associatedAccount1);
        setAssociatedBudget(associatedBudget);
        setAmount(amount);
        setReason(reason);
    }

    // for regular withdrawals
    HistoryElement(HistoryType transactionType, String associatedAccount1, Double amount, String reason) {
        setTransactionType(transactionType);
        setAssociatedAccount1(associatedAccount1);
        setAmount(amount);
        setReason(reason);
    }

    // for creation or reset of an account or budget
    HistoryElement(HistoryType transactionType, String associatedAccount, Double amount) {
        setTransactionType(transactionType);
        setAssociatedAccount1(associatedAccount);
        setAmount(amount);
    }

    // for database json purposes
    HistoryElement() {}

    // setters
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    public void setAssociatedAccount1(String associatedAccount1) {
        this.associatedAccount1 = associatedAccount1;
    }
    public void setAssociatedAccount2(String associatedAccount2) {
        this.associatedAccount2 = associatedAccount2;
    }
    public void setAssociatedBudget(String associatedBudget) {
        this.associatedBudget = associatedBudget;
    }
    public void setTransactionType(HistoryType transactionType) {
        this.transactionType = transactionType;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    // getters
    public Double getAmount() {
        return amount;
    }
    public HistoryType getTransactionType() {
        return transactionType;
    }
    public String getAssociatedAccount1() {
        return associatedAccount1;
    }
    public String getAssociatedAccount2() {
        return associatedAccount2;
    }
    public String getAssociatedBudget() {
        return associatedBudget;
    }
    public String getReason() {
        return reason;
    }

    // other functions for ease of use
    public String retrieveHistoryString() {
        switch (getTransactionType()) {
            case Deposit:
                return String.format(Locale.US, "Deposited $%.2f into account \"%s\" for %s", getAmount(), getAssociatedAccount1(), getReason());
            case Withdrawal:
                return String.format(Locale.US, "Withdrew $%.2f from account \"%s\" for %s", getAmount(), getAssociatedAccount1(), getReason());
            case Transfer:
                return String.format(Locale.US, "Transferred $%.2f from account \"%s\" into account \"%s\"", getAmount(), getAssociatedAccount1(), getAssociatedAccount2());
            case Created: {
                if (getAmount() < 0)
                    return String.format(Locale.US, "Created account \"%s\" with an initial balance of $(%.2f)", getAssociatedAccount1(), -getAmount());
                else
                    return String.format(Locale.US, "Created account \"%s\" with an initial balance of $%.2f", getAssociatedAccount1(), getAmount());
            }
            case Reset: {
                if (getAmount() < 0) {
                    return String.format(Locale.US, "Reset history logs when balance was $(%.2f)", -getAmount());
                } else {
                    return String.format(Locale.US, "Reset history logs when balance was $%.2f", getAmount());
                }
            }
            case BudgetReset: {
                return String.format(Locale.US, "Reset budget \"%s\" back to $%.2f", getAssociatedAccount1(), getAmount());
            }
            case BudgetCreate: {
                return String.format(Locale.US, "Created budget \"%s\" for $%.2f", getAssociatedAccount1(), getAmount());
            }
            case BudgetChanged: {
                return String.format(Locale.US, "Changed budget amount by %.2f", getAmount());
            }
            case ManuallyEntered: {
                return String.format(Locale.US, "Manually added an expense of \"%.2f\" to budget \"%s\" for %s", getAmount(), getAssociatedAccount1(), getReason());
            }
            default:
                return null;
        }
    }
}
