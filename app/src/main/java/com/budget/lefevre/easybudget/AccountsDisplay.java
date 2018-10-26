package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountsDisplay extends MenuOptions {
    private static final int EXIT_ACTIVITY = 101;

    private boolean exit;
    private AccountListAdapter accountListAdapter;
    private ListView listView;
    private TextView textView;

    private FirebaseDatabase myDatabase;
    private DatabaseReference myReference;
    private FirebaseUser user;

    // onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts_display);

        exit = false;

        listView = (ListView) findViewById(R.id.AD_accountsList);
        textView = (TextView) findViewById(R.id.AD_allAccounts);

        accountListAdapter = new AccountListAdapter(getApplicationContext());
        listView.setAdapter(accountListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openUpAccountDisplay(i);
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        myDatabase = FirebaseDatabase.getInstance();
        if (user != null) {
            // listener for the accounts
            myReference = myDatabase.getReference().child(user.getUid()).child("Accounts");
            myReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    accountListAdapter.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d("DataCapture", ds.getKey());
                        try {
                            String name = String.valueOf(ds.child("accountName").getValue());
                            Double balance = Double.valueOf(String.valueOf(ds.child("balanceDisplay").getValue()));

                            Account account = new Account(name, balance);
                            Log.d("DataCapture", account.getAccountName() + ":" + account.getBalanceDisplay());
                            accountListAdapter.add(account);
                        } catch (Exception e) {
                            e.printStackTrace();
                            new ToastMessages(AccountsDisplay.this, "Error retrieving Account information");
                        }
                    }

                    Log.d("DataCapture", "notifying adapter");
                    accountListAdapter.notifyDataSetChanged();
                    updateTotal();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            updateTotal();
        }
    }

    // updates total amount of one's assets
    private void updateTotal() {
        Double sumOfAllAccounts = 0.00;
        for (Account account : accountListAdapter.retrieveAccounts()) {
            sumOfAllAccounts += account.getBalanceDisplay();
            Log.d("DataCapture", "getting amounts");
        }

        String total;
        if (sumOfAllAccounts < 0) {
            total = String.format(Locale.US, "$(%.2f)", -sumOfAllAccounts);
        } else {
            total = String.format(Locale.US, "$%.2f", sumOfAllAccounts);
        }

        textView.setText(total);
    }

    // onClick method for list view items
    private void openUpAccountDisplay(int index) {
        Log.d("DataCapture", "Entering single account view");

        Intent intent = new Intent(this, SingleAccountDisplay.class);
        String name = accountListAdapter.getItem(index).getAccountName();
        Bundle bundle = new Bundle();
        bundle.putString("account", name);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // exit app on double back press
    @Override
    public void onBackPressed() {
        // do nothing
        if (exit) {
            setResult(EXIT_ACTIVITY);
            finish();
        }

        exit = true;
        new ToastMessages(this, "Press back button again to exit App");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                exit = false;
            }
        }, 2000);
    }

    // setUp for menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ad_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}

