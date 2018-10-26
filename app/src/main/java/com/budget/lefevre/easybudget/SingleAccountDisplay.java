package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SingleAccountDisplay extends MenuOptions {
    private static int DELETE_SINGLE_ACCOUNT;
    private static int CHANGE_HISTORY;

    private Account account;
    private String name;
    private TextView accountView;
    private TextView amountView;
    private ListView historyView;
    private HistoryListAdapter adapter;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DatabaseReference transferReference;
    private DatabaseReference budgetReference;
    private DataSnapshot transferSnapshot;
    private DataSnapshot budgetSnapshot;
    private FirebaseUser user;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_account_display);

        // check for current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "No user currently signed in");
            finish();
            return;
        }
        userID = user.getUid();

        // setup ints for results in activities
        DELETE_SINGLE_ACCOUNT = getBaseContext().getResources().getInteger(R.integer.DELETE_SINGLE_ACCOUNT);
        CHANGE_HISTORY = getBaseContext().getResources().getInteger(R.integer.CHANGE_HISTORY);

        // setup views for use
        accountView = (TextView) findViewById(R.id.SAD_account);
        amountView = (TextView) findViewById(R.id.SAD_amount);
        historyView = (ListView) findViewById(R.id.SAD_historyListView);

        // setup list view
        adapter = new HistoryListAdapter(getApplicationContext());
        historyView.setAdapter(adapter);
        historyView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // method for editing history elements
                Intent intent;
                HistoryElement historyElement = adapter.getItem(i);
                new LogThis(2, "onItemLongClickEnabled");

                if (historyElement.getTransactionType() == HistoryType.Deposit ||
                        historyElement.getTransactionType() == HistoryType.Withdrawal) {
                    intent = new Intent(SingleAccountDisplay.this, HistoryEditActivity.class);
                } else if (historyElement.getTransactionType() == HistoryType.Transfer) {
                    intent = new Intent(SingleAccountDisplay.this, HistoryEditTransfer.class);
                } else {
                    return false;
                }

                new LogThis(2, "Preparing the intent");
                Bundle bundle = new Bundle();
                bundle.putInt("index", i);
                bundle.putString("type", String.valueOf(historyElement.getTransactionType()));
                bundle.putString("reason", historyElement.getReason());
                bundle.putString("associatedAccount1", historyElement.getAssociatedAccount1());
                bundle.putString("associatedAccount2", historyElement.getAssociatedAccount2());
                bundle.putString("associatedBudget", historyElement.getAssociatedBudget());
                bundle.putDouble("amount", historyElement.getAmount());
                intent.putExtras(bundle);

                SingleAccountDisplay.this.startActivityForResult(intent, CHANGE_HISTORY);

                return true;
            }
        });

        // grab information to be used for displaying the budget
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        name = bundle.getString("account");
        if (name == null) {
            new ToastMessages(this, "Error reading account name", true);
            finish();
        }

        // grab information about the account to be displayed
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Accounts").child(name);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                account = dataSnapshot.getValue(Account.class);
                if (account == null) {
                    new ToastMessages(SingleAccountDisplay.this, "failed to read account information");
                    finish();
                    return;
                }

                accountView.setText(account.getAccountName());

                String stringBalance;
                if (account.getBalanceDisplay() < 0) {
                    stringBalance = String.format(Locale.US, "$(%.2f)", -account.getBalanceDisplay());
                } else {
                    stringBalance = String.format(Locale.US, "$%.2f", account.getBalanceDisplay());
                }

                amountView.setText(stringBalance);
                adapter.setHistoryElements(account.getHistory());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // grab data about other accounts in-case of editing a transfer item
        transferReference = database.getReference().child(userID).child("Accounts");
        transferReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transferSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // grab data about budgets in case an edited item is in a budget too
        budgetReference = database.getReference().child(userID).child("Budgets");
        budgetReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                budgetSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // enter activity for deposit/withdrawal
    public void depositWithdraw(View view) {
        Intent intent = new Intent(this, DepositWithdraw.class);
        intent.putExtra("account", account.getAccountName());
        startActivity(intent);
    }

    // delete an account
    public void deleteSingleAccount(View view) {
        Intent intent = new Intent(this, ConfirmationPopUp.class);

        intent.putExtra("confirm/Deny", String.format(Locale.US, "delete \"%s\" account", account.getAccountName()));
        startActivityForResult(intent, DELETE_SINGLE_ACCOUNT);
    }

    // activity for transferring money
    public void transferMoney(View view) {
        Intent intent = new Intent(this, TransferMoneyActivity.class);
        startActivity(intent);
    }

    // create menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sad_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // handling results of called activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user wants to delete their account
        if (requestCode == DELETE_SINGLE_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                new ToastMessages(this, String.format(Locale.US, "Deleted \"%s\" account", account.getAccountName()));
                database.getReference().child(userID).child("Accounts").child(account.getAccountName()).removeValue();
                finish();
            }
        }

        // user wants to change their account history
        if (requestCode == CHANGE_HISTORY) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                Integer index = bundle.getInt("index");

                // retrieve information about the item to be edited
                HistoryType historyType;
                String type = bundle.getString("type");
                if (type == null)
                    return;

                switch (type) {
                    case "Deposit":
                        historyType = HistoryType.Deposit;
                        break;
                    case "Withdraw": {
                        historyType = HistoryType.Withdrawal;
                        break;
                    }
                    case "Transfer": {
                        historyType = HistoryType.Transfer;
                        break;
                    }
                    default:
                        return;
                }

                String reason = bundle.getString("reason");
                String associatedAccount1 = bundle.getString("associatedAccount1");
                String associatedAccount2 = bundle.getString("associatedAccount2");
                Double amount = bundle.getDouble("amount");

                // is this a transfer item?
                if (historyType == HistoryType.Transfer) {
                    // ensure it was a transfer
                    if (associatedAccount1 == null ||
                            associatedAccount2 == null) {
                        return;
                    }
                    new LogThis(2, "Type is transfer");

                    // determine which is the other account that needs to be edited
                    Account transferAccount;
                    if (account.getAccountName().equals(associatedAccount1)) {
                        transferAccount = transferSnapshot.child(associatedAccount2).getValue(Account.class);
                    } else {
                        transferAccount = transferSnapshot.child(associatedAccount1).getValue(Account.class);
                    }

                    // find the item to be edited in the other account
                    new LogThis(2, "finding index");
                    int transferIndex;
                    HistoryElement historyElement = new HistoryElement(historyType, associatedAccount1, associatedAccount2, amount);
                    if (transferAccount == null) {
                        return;
                    } else {
                        transferIndex = transferAccount.findForEditTransfer(adapter.getItem(index));
                        if (transferIndex == -1) {
                            return;
                        }
                    }

                    // replace the old data and save it to the database
                    new LogThis(2, "index found");
                    adapter.setIndexOfTo(index, historyElement);
                    account.setHistory(adapter.getHistoryElements());
                    transferAccount.set(transferIndex, historyElement);
                    database.getReference().child(userID).child("Accounts").child(account.getAccountName()).setValue(account);
                    database.getReference().child(userID).child("Accounts").child(transferAccount.getAccountName()).setValue(transferAccount);
                } else {
                    // not a transfer item
                    adapter.setIndexOfTo(index, new HistoryElement(historyType, associatedAccount1, amount, reason));
                    account.setHistory(adapter.getHistoryElements());
                    database.getReference().child(userID).child("Accounts").child(account.getAccountName()).setValue(account);
                }
            }
        }
    }
}

