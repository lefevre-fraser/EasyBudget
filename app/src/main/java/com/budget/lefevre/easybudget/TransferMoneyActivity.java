package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

public class TransferMoneyActivity extends MenuOptions {
    private static int ADD_TO_BUDGET;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DatabaseReference budgets;
    private DataSnapshot budgetSnapshot;
    private FirebaseUser user;
    private String userID;

    private List<String> accountDisplay;
    private List<Account> accounts;
    private Spinner accountSpinner1;
    private Spinner accountSpinner2;
    private EditText transferAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_money);

        // check for user signed in
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "no user currently signed in");
            finish();
            return;
        }
        userID = user.getUid();

        // setup ints for results
        ADD_TO_BUDGET = getBaseContext().getResources().getInteger(R.integer.ADD_TO_BUDGET);

        // set up views for use
        accountSpinner1 = (Spinner) findViewById(R.id.TM_nameSpinner);
        accountSpinner2 = (Spinner) findViewById(R.id.TM_nameSpinner2);
        transferAmount = (EditText) findViewById(R.id.TM_transferAmount);

        accountDisplay = new ArrayList<>();
        accounts = new ArrayList<>();

        // grab account information to use for transfer activity
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Accounts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                accountDisplay.clear();
                accounts.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        Account account = ds.getValue(Account.class);
                        if (account == null)
                            continue;

                        if (account.getBalanceDisplay() < 0) {
                            accountDisplay.add(String.format(Locale.US, "%s $(%.2f)", account.getAccountName(), -account.getBalanceDisplay()));
                        } else {
                            accountDisplay.add(String.format(Locale.US, "%s $%.2f", account.getAccountName(), account.getBalanceDisplay()));
                        }

                        accounts.add(account);
                    } catch (Exception e) {
                        e.printStackTrace();
                        new ToastMessages(TransferMoneyActivity.this, "Error retrieving Account information");
                    }
                }

                ArrayAdapter<String> spinnerChoices =
                        new ArrayAdapter<String>(TransferMoneyActivity.this,
                                R.layout.layout_simple_spinner, accountDisplay);
                accountSpinner1.setAdapter(spinnerChoices);
                accountSpinner2.setAdapter(spinnerChoices);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        budgets = database.getReference().child(userID).child("Budgets");
        budgets.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        budgetSnapshot = dataSnapshot;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // complete transfer activity
    public void completeTransfer(View view) {
        // check for different accounts
        if (accountSpinner1.getSelectedItem().equals(accountSpinner2.getSelectedItem())) {
            new ToastMessages(this, "Must choose two different accounts");
            return;
        }

        // check for valid amount to transfer
        Double amount;
        try {
            amount = Double.valueOf(transferAmount.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            new ToastMessages(this, "must enter a valid amount");
            return;
        }
        if (amount == 0.00) {
            new ToastMessages(this, "Amount must be greater than $0.00");
            return;
        }

        // gather account information for determining whether to add to budget or not
        Integer location2 = accountSpinner2.getSelectedItemPosition();
        Account account2 = accounts.get(location2);

        // let user add to budget or not
        if (account2.getBalanceDisplay() < 0) {
            Intent intent = new Intent(this, AddToBudget.class);
            startActivityForResult(intent, ADD_TO_BUDGET);
            return;
        }

        // preform a normal transfer
        Integer location1 = accountSpinner1.getSelectedItemPosition();
        Account account1 = accounts.get(location1);

        String transferer = account1.getAccountName();
        String transferee = account2.getAccountName();

        HistoryElement element = new HistoryElement(HistoryType.Transfer, transferer, transferee, amount);
        account1.addHistoryElement(element);
        account2.addHistoryElement(element);

        new ToastMessages(this, account1.retrieveFirstElement().retrieveHistoryString(), true);

        reference.child(transferer).setValue(account1);
        reference.child(transferee).setValue(account2);

        finish();
    }

    // add menu to activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tm_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TO_BUDGET) {
            // general information for transfer
            Double amount = Double.valueOf(transferAmount.getText().toString());

            Integer location2 = accountSpinner2.getSelectedItemPosition();
            Account account2 = accounts.get(location2);

            Integer location1 = accountSpinner1.getSelectedItemPosition();
            Account account1 = accounts.get(location1);

            String transferer = account1.getAccountName();
            String transferee = account2.getAccountName();

            HistoryElement element = new HistoryElement(HistoryType.Transfer, transferer, transferee, amount);

            // preform transfer and show on a budget
            if (resultCode == RESULT_OK) {
                // gather budget information
                String associatedBudget = data.getStringExtra("associatedBudget");
                element.setAssociatedBudget(associatedBudget);
                if (!budgetSnapshot.hasChild(associatedBudget)) {
                    new ToastMessages(this, "budget no longer exists");
                    return;
                }

                Budget budget;
                try {
                    budget = budgetSnapshot.child(associatedBudget).getValue(Budget.class);
                    if (budget == null) {
                        new ToastMessages(this, "error retrieving budget data");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new ToastMessages(this, "error retrieving budget data");
                    return;
                }

                // save changes to database
                budget.addToHistory(element);
                account1.addHistoryElement(element);
                account2.addHistoryElement(element);
                new ToastMessages(this, account1.retrieveFirstElement().retrieveHistoryString(), true);
                budgets.child(associatedBudget).setValue(budget);
                reference.child(transferer).setValue(account1);
                reference.child(transferee).setValue(account2);
                finish();
                return;
            }

            if (resultCode == RESULT_CANCELED) {
                // preform normal transfer
                account1.addHistoryElement(element);
                account2.addHistoryElement(element);
                new ToastMessages(this, account1.retrieveFirstElement().retrieveHistoryString(), true);
                reference.child(transferer).setValue(account1);
                reference.child(transferee).setValue(account2);
                finish();
                return;
            }
        }
    }
}
