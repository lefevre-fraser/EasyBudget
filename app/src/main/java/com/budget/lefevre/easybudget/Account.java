package com.budget.lefevre.easybudget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mp3le on 3/25/2018.
 */

public class Account {
    private String accountName;
    private Double balanceDisplay;
    private List<HistoryElement> history;

    // Constructor for new accounts and account list display
    Account(String name, Double balance) {
        if (name.equals(""))
            setAccountName("UnknownName");
        else
            setAccountName(name);

        history = new ArrayList<>();
        HistoryElement element = new HistoryElement(HistoryType.Created, getAccountName(), balance);
        addHistoryElement(element);
    }

//    Account(String name, List<HistoryElement> oldHistory) {
//        if (name.equals(""))
//            setAccountName("UnknownName");
//        else
//            setAccountName(name);
//
//        setHistory(oldHistory);
//    }

    // constructor for database purposes
    Account() {
        setHistory(new ArrayList<HistoryElement>());
    }

    // setters
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public void setHistory(List<HistoryElement> history) {
        this.history = history;
        setBalanceDisplay(retrieveBalance());
    }
    public void setBalanceDisplay(Double balanceDisplay) {
        this.balanceDisplay = balanceDisplay;
    }

    // getters
    public String getAccountName() {
        return accountName;
    }
    public List<HistoryElement> getHistory() {
        return history;
    }
    public Double getBalanceDisplay() {
        return balanceDisplay;
    }

    // other functions for account personality
    public Double retrieveBalance() {
        Double balance = 0.00;
        for (HistoryElement historyElement : history) {
            switch (historyElement.getTransactionType()) {
                case Deposit:
                    balance += historyElement.getAmount();
                    break;
                case Withdrawal:
                    balance -= historyElement.getAmount();
                    break;
                case Created:
                    balance += historyElement.getAmount();
                    break;
                case Reset:
                    balance += historyElement.getAmount();
                    break;
                case Transfer: {
                    if (historyElement.getAssociatedAccount1().equals(getAccountName()))
                        balance -= historyElement.getAmount();
                    else
                        balance += historyElement.getAmount();
                    break;
                }
                default:
                    break;
            }
        }

        return balance;
    }

    public void addHistoryElement(HistoryElement historyElement) {
        getHistory().add(0, historyElement);
        setBalanceDisplay(retrieveBalance());
    }

    public HistoryElement retrieveLastElement() {
        if (history.size() > 0) {
            return history.get(history.size() - 1);
        }

        return null;
    }

    public HistoryElement retrieveFirstElement() {
        if (history.size() > 0) {
            return history.get(0);
        }

        return null;
    }

    public int findForEditTransfer(HistoryElement historyElement) {
        int index = 0;
        for (HistoryElement element: history) {
            if (element.getTransactionType() == historyElement.getTransactionType() &&
                    element.getAmount().equals(historyElement.getAmount()) &&
                    element.getAssociatedAccount1().equals(historyElement.getAssociatedAccount1()) &&
                    element.getAssociatedAccount2().equals(historyElement.getAssociatedAccount2())) {
                return index;
            } else {
                ++index;
            }
        }

        return -1;
    }

    public void set(int transferIndex, HistoryElement historyElement) {
        history.set(transferIndex, historyElement);
        setBalanceDisplay(retrieveBalance());
    }

    public void resetAccountHistory() {
        getHistory().clear();
        HistoryElement element = new HistoryElement(HistoryType.Reset, getAccountName(), getBalanceDisplay());
        addHistoryElement(element);
        setBalanceDisplay(retrieveBalance());
    }
}

