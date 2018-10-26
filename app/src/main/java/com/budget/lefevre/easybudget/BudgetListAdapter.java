package com.budget.lefevre.easybudget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetListAdapter extends BaseAdapter {
    private List<Budget> budgets;
    private Context context;

    // constructor for adapter
    BudgetListAdapter(Context context) {
        setContext(context);
        setBudgets(new ArrayList<Budget>());
    }

    // necessary adapter functions
    @Override
    public int getCount() {
        return budgets.size();
    }

    @Override
    public Budget getItem(int i) {
        return budgets.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.layout_account_amounts, null);

        TextView budgetName = (TextView) view.findViewById(R.id.AA_accountName);
        TextView budgetAmount = (TextView) view.findViewById(R.id.AA_accountAmount);

        budgetName.setText(budgets.get(i).getBudgetName());
        Double balance = budgets.get(i).getBalanceLeft();
        String stringBalance;

        if (balance < 0)
            stringBalance = String.format(Locale.US, "$(%.2f)", -balance);
        else
            stringBalance = String.format(Locale.US, "$%.2f", balance);
        budgetAmount.setText(stringBalance);

        return view;
    }

    // setters
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    // getters
    public List<Budget> getBudgets() {
        return budgets;
    }

    // additional functions for adapter functionality
    public void add(Budget budget) {
        getBudgets().add(budget);
    }
    public void clear() {
        getBudgets().clear();
    }
}
