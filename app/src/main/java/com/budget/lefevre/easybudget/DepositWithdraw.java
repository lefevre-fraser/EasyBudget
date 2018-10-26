package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

public class DepositWithdraw extends MenuOptions {
    private static final int ADD_TO_BUDGET = 200;
    private DataSnapshot budgetsData;
    private Account account;
    private String name;
    private TextView balanceView;
    private EditText amountWD;
    private EditText reason;
    private Spinner actionSpinner;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DatabaseReference budgetsReference;
    private FirebaseUser user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_withdraw);

        // tet up views for use
        reason = (EditText) findViewById(R.id.DW_reason);
        balanceView = (TextView) findViewById(R.id.DW_balanceAmount);
        amountWD = (EditText) findViewById(R.id.DW_amount);
        actionSpinner = (Spinner) findViewById(R.id.DW_spinner);

        String options[] = {"Withdraw", "Deposit"};
        ArrayAdapter<String> spinnerChoices = new ArrayAdapter<String>(this, R.layout.layout_simple_spinner, options);
        actionSpinner.setAdapter(spinnerChoices);

        // get account name to be edited
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("account");
        if (name == null) {
            new ToastMessages(this, "Error reading account");
            return;
        }

        // check for current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "no user currently logged in");
            finish();
            return;
        }
        userID = user.getUid();

        // listener for a single account
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Accounts").child(name);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                account = dataSnapshot.getValue(Account.class);
                if (account == null)
                    return;

                Double balance = account.getBalanceDisplay();

                String stringBalance;
                if (balance < 0) {
                    stringBalance = String.format(Locale.US, "Current: $(%.2f)", -balance);
                } else {
                    stringBalance = String.format(Locale.US, "Current: $%.2f", balance);
                }
                balanceView.setText(stringBalance);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // listener for budgets if adding expense to budget is desired
        budgetsReference = database.getReference().child(userID).child("Budgets");
        budgetsReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        budgetsData = dataSnapshot;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // on click method for completing the transaction
    public void completeAction(View view) {
        Double balance;
        try {
            balance = Double.valueOf(String.valueOf(amountWD.getText()));
        } catch (Exception e) {
            new ToastMessages(this, "Enter a valid amount");
            return;
        }

        String reasonString = String.valueOf(reason.getText());
        // if user wants to make a deposit
        if (actionSpinner.getSelectedItem().toString().equals("Deposit")) {
            account.addHistoryElement(new HistoryElement(HistoryType.Deposit, account.getAccountName(), balance, reasonString));
            new ToastMessages(this, account.retrieveFirstElement().retrieveHistoryString(), true);
            reference.setValue(account);
            finish();
        } else {
            // if users makes withdrawal
            Intent intent = new Intent(this, AddToBudget.class);
            startActivityForResult(intent, ADD_TO_BUDGET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // determine if adding to budget is required
        if (requestCode == ADD_TO_BUDGET) {
            if (resultCode == RESULT_OK) {
                // preparing to add information to a budget
                Double balance = Double.valueOf(String.valueOf(amountWD.getText()));
                String reasonString = String.valueOf(reason.getText());
                String budgetName = data.getStringExtra("associatedBudget");
                HistoryElement historyElement = new HistoryElement(HistoryType.Withdrawal, account.getAccountName(), budgetName, balance, reasonString);
                account.addHistoryElement(historyElement);

                // add expense to budget and update database
                Budget budget = budgetsData.child(budgetName).getValue(Budget.class);
                if (budget == null) {
                    new ToastMessages(this, "Budget no longer Exists");
                    return;
                }
                budget.addToHistory(historyElement);
                budgetsReference.child(budgetName).setValue(budget);

                reference.setValue(account);
                new ToastMessages(this, account.retrieveFirstElement().retrieveHistoryString(), true);
                finish();
                return;
            } else if (resultCode == RESULT_CANCELED) {
                // error completing action
                if (data == null) {
                    new ToastMessages(this, "Withdrawal canceled");
                    return;
                }

                // user does not wish to add expense to a budget
                if (data.getStringExtra("associatedBudget").equals("NoAssociatedBudget")) {
                    Double balance = Double.valueOf(String.valueOf(amountWD.getText()));
                    String reasonString = String.valueOf(reason.getText());
                    account.addHistoryElement(new HistoryElement(HistoryType.Withdrawal, account.getAccountName(), balance, reasonString));
                    new ToastMessages(this, account.retrieveFirstElement().retrieveHistoryString(), true);
                    reference.setValue(account);
                    finish();
                    return;
                }
            }
        }
    }

    // setup of menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dw_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
