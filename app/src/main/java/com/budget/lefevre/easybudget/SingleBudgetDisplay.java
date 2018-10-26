package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

import java.util.Locale;

public class SingleBudgetDisplay extends AppCompatActivity {
    private static final int DELETE_SINGLE_ACCOUNT = 110;
    private static final int CHANGE_AMOUNT = 210;
    private static final int MANUAL_ADD = 310;

    private String name;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;
    private String userID;
    private Budget budget;

    private TextView budgetName;
    private TextView budgetAmount;
    private TextView balanceLeft;
    private ListView historyList;
    private HistoryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_budget_display);

        // grab name of budget to be viewed
        name = getIntent().getStringExtra("budgetName");

        // check for current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "No user currently sign in");
            finish();
        }
        userID = user.getUid();

        // setup views for use
        budgetName = (TextView) findViewById(R.id.SBD_budgetName);
        budgetAmount = (TextView) findViewById(R.id.SBD_budgetAmount);
        balanceLeft = (TextView) findViewById(R.id.SBD_balanceLeft);
        historyList = (ListView) findViewById(R.id.SBD_historyView);
        adapter = new HistoryListAdapter(getApplicationContext());
        historyList.setAdapter(adapter);

        // acquire budget's data
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Budgets").child(name);
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        adapter.clear();
                        budget = dataSnapshot.getValue(Budget.class);
                        if (budget == null) {
                            new ToastMessages(SingleBudgetDisplay.this, "Budget no longer Exists");
                            finish();
                            return;
                        }

                        // display budget properties
                        budgetName.setText(budget.getBudgetName());
                        String amount = String.format(Locale.US, "Budget Amount: $%.2f", budget.getBudgetAmount());
                        budgetAmount.setText(amount);
                        String balance;
                        if (budget.getBalanceLeft() < 0) {
                            balance = String.format(Locale.US, "Amount left: $(%.2f)", -budget.getBalanceLeft());
                        } else {
                            balance = String.format(Locale.US, "Amount left: $%.2f", budget.getBalanceLeft());
                        }
                        balanceLeft.setText(balance);

                        adapter.setHistoryElements(budget.getHistory());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    // reset a budget
    public void SBD_onResetBudgetClick(View view) {
        budget.resetBudget();
        reference.setValue(budget);

        new ToastMessages(this, "Reset Budget");
    }

    // delete a budget
    public void SBD_onDeleteButtonClick(View view) {
        Intent intent = new Intent(this, ConfirmationPopUp.class);
        intent.putExtra("confirm/Deny", "delete this budget?");
        startActivityForResult(intent, DELETE_SINGLE_ACCOUNT);
    }

    // change budget amount
    public void SBD_onChangeAmountButtonClick(View view) {
        Intent intent = new Intent(this, SimpleValueChange.class);
        startActivityForResult(intent, CHANGE_AMOUNT);
    }

    // add expenses manually to a budget
    public void SBD_onManualAddButtonClick(View view) {
        Intent intent = new Intent(this, ManuallyAddBudgetHistory.class);
        startActivityForResult(intent, MANUAL_ADD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // delete the current budget
        if (requestCode == DELETE_SINGLE_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                reference.removeValue();

                new ToastMessages(this, "Deleting Budget");
                finish();
                return;
            }
        }

        // change the budget amount - result
        if (requestCode == CHANGE_AMOUNT) {
            if (resultCode == RESULT_OK) {
                Double amount = data.getDoubleExtra("amount", 0);
                if (amount == 0) {
                    new ToastMessages(this, "Change failed to take place");
                    return;
                } else {
                    budget.addToHistory(new HistoryElement(HistoryType.BudgetChanged, budget.getBudgetName(), (amount - budget.getBudgetAmount())));
                    budget.setBudgetAmount(amount);
                    Budget newBudget = new Budget(budget.getBudgetName(), budget.getBudgetAmount(), budget.getHistory());
                    reference.setValue(newBudget);
                    return;
                }
            }
        }

        // manually add expenses to budget - result
        if (requestCode == MANUAL_ADD) {
            if (resultCode == RESULT_OK) {
                Double amount = data.getDoubleExtra("amount", 0);
                if (amount == 0) {
                    new ToastMessages(this, "Change failed to take place");
                    return;
                } else {
                    String reason = data.getStringExtra("reason");
                    budget.addToHistory(new HistoryElement(HistoryType.ManuallyEntered, budget.getBudgetName(), amount, reason));
                    reference.setValue(budget);
                    return;
                }
            }
        }
    }
}
