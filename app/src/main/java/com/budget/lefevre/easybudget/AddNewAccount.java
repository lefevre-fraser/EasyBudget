package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddNewAccount extends MenuOptions {
    private static int ADD_TO_BUDGET;
    private TextView accountName;
    private TextView accountStartAmount;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private DataSnapshot accounts;
    private DataSnapshot budgets;
    private FirebaseUser user;
    private String userID;

    // on create method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_account);
        ADD_TO_BUDGET = getBaseContext().getResources().getInteger(R.integer.ADD_TO_BUDGET);

        // setup views for use
        accountName = (TextView) findViewById(R.id.ANA_newAccountName);
        accountStartAmount = (TextView) findViewById(R.id.ANA_startingBalance);

        // check for signed in and set up database
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "no user currently logged in");
            finish();
            return;
        }
        userID = user.getUid();

        // listener for the account to prevent account name duplication
        reference.child(userID).child("Accounts")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                accounts = dataSnapshot;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        // listener for budgets for adding new account to budget if initial balance is negative
        reference.child(userID).child("Budgets")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                budgets = dataSnapshot;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );
    }

    // create account if account name is not already in existence
    public void finishCreatingAccount(View view) {
        String name = accountName.getText().toString();
        if (name.equals("")) {
            new ToastMessages(this,"Must Enter An Account Name");
            return;
        } else if (accounts.hasChild(name)) {
            new ToastMessages(this, "Account name already exists");
            return;
        }

        Double initialAmount;
        try {
            initialAmount = Double.valueOf(accountStartAmount.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            new ToastMessages(this, "Must enter a valid amount");
            return;
        }

        if (initialAmount < 0) {
            new LogThis(2, "initial amount is less than 0");
            Intent intent = new Intent(this, AddToBudget.class);
            startActivityForResult(intent, ADD_TO_BUDGET);
            return;
        }

        Account account = new Account(name, initialAmount);
        firebaseDatabase.getReference().child(userID).child("Accounts").child(name).setValue(account);
        new ToastMessages(this, account.retrieveLastElement().retrieveHistoryString());
        finish();
    }

    // setUp menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ana_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // add a new account as an expense to a budget
        if (requestCode == ADD_TO_BUDGET) {
            String associatedBudget = data.getStringExtra("associatedBudget");
            if (associatedBudget == null) {
                new ToastMessages(this, "failed to add the account as an expense to a budget");
                return;
            }

            Double initial = Double.valueOf(accountStartAmount.getText().toString());
            String name = accountName.getText().toString();
            Account account;

            // user wants an associated budget
            if (resultCode == RESULT_OK) {
                account = new Account();
                account.setAccountName(name);
                HistoryElement historyElement = new HistoryElement(HistoryType.Created, name, initial);
                historyElement.setAssociatedBudget(associatedBudget);
                account.addHistoryElement(historyElement);

                // determine if budget is still in existence
                if (!budgets.hasChild(associatedBudget)) {
                    new ToastMessages(this, "budget no longer exists");
                    return;
                }

                // retrieve budget information
                Budget budget = budgets.child(associatedBudget).getValue(Budget.class);
                if (budget == null) {
                    new ToastMessages(this, "Could not retrieve budget information");
                    return;
                }
                budget.addToHistory(historyElement);

                // update database
                firebaseDatabase.getReference().child(userID).child("Budgets").child(associatedBudget).setValue(budget);
                firebaseDatabase.getReference().child(userID).child("Accounts").child(name).setValue(account);
                finish();
                return;
            }

            if (resultCode == RESULT_CANCELED) {
                // no budget was selected
                if (associatedBudget.equals("NoAssociatedBudget")) {
                    account = new Account(name, initial);
                    firebaseDatabase.getReference().child(userID).child("Accounts").child(name).setValue(account);
                    finish();
                    return;
                }
            }
        }
    }
}
