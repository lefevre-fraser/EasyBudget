package com.budget.lefevre.easybudget;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class CreateNewBudget extends MenuOptions {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DataSnapshot currentBudgets;
    private FirebaseUser user;
    private String userID;

    private EditText budgetName;
    private EditText budgetAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_budget);

        // setup views for use
        budgetAmount = (EditText) findViewById(R.id.CNB_budgetAmount);
        budgetName = (EditText) findViewById(R.id.CNB_budgetName);

        // check for current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "No user currently logged in");
            finish();
            return;
        }
        userID = user.getUid();

        // listener for budgets to see if already exists
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        reference.child(userID).child("Budgets")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                currentBudgets = dataSnapshot;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );
    }

    // create budget with error handling
    public void createBudget(View view) {
        if (String.valueOf(budgetName.getText()).equals("")) {
            new ToastMessages(this, "Must enter a budget name");
            return;
        } else if (String.valueOf(budgetAmount.getText()).equals("")) {
            new ToastMessages(this, "Must enter a budget amount");
            return;
        } else if (Double.valueOf(String.valueOf(budgetAmount.getText())) == 0.0) {
            new ToastMessages(this, "Budget value cannot be $0.00");
            return;
        }

        Double budgetDouble = Double.valueOf(String.valueOf(budgetAmount.getText()));
        String budgetNameString = String.valueOf(budgetName.getText());
        if (currentBudgets.hasChild(budgetNameString)) {
            new ToastMessages(this, "Budget name already exists");
            return;
        } else if (budgetNameString.equals("")) {
            new ToastMessages(this, "Must enter an budget name");
            return;
        }

        Budget budget = new Budget(budgetNameString, budgetDouble);
        reference.child(userID).child("Budgets").child(budgetNameString).setValue(budget);
        new ToastMessages(this, budget.retrieveFirstElement().retrieveHistoryString(), true);
        finish();
    }

    // setup menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cnb_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
