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

public class AccountListAdapter extends BaseAdapter {
    private List<Account> accounts;
    private Context context;

    // constructor for adapter
    AccountListAdapter(Context context) {
        this.accounts = new ArrayList<>();
        this.context = context;
    }

    // adapter functions
    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Account getItem(int i) {
        return accounts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.layout_account_amounts, null);

        TextView accountName = (TextView) view.findViewById(R.id.AA_accountName);
        TextView accountAmount = (TextView) view.findViewById(R.id.AA_accountAmount);

        accountName.setText(accounts.get(i).getAccountName());
        Double balance = accounts.get(i).getBalanceDisplay();
        String stringBalance;

        if (balance < 0)
            stringBalance = String.format(Locale.US, "$(%.2f)", -balance);
        else
            stringBalance = String.format(Locale.US, "$%.2f", balance);
        accountAmount.setText(stringBalance);

        return view;
    }

    // functions for adapter functionality
    public List<Account> retrieveAccounts() {
        return accounts;
    }
    public void add(Account account) {
        accounts.add(account);
    }
    public void clear() {
        accounts.clear();
    }
}
