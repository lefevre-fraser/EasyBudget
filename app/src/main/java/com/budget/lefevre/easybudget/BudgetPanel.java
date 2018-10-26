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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetPanel extends MenuOptions {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;
    private String userID;

    private BudgetListAdapter adapter;
    private TextView totalBalanceLeft;
    private ListView budgetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_panel);

        // set up for activity functionality
        totalBalanceLeft = (TextView) findViewById(R.id.BP_totalBudgetLeft);
        budgetList = (ListView) findViewById(R.id.BP_budgetList);

        adapter = new BudgetListAdapter(this);
        budgetList.setAdapter(adapter);

        // check for current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "There is no current user");
            finish();
        }
        userID = user.getUid();

        // listener for budgets for list view
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Budgets").getRef();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        String budgetName = String.valueOf(ds.child("budgetName").getValue());
                        Double budgetAmount = Double.valueOf(String.valueOf(ds.child("budgetAmount").getValue()));
                        Double balanceLeft = Double.valueOf(String.valueOf(ds.child("balanceLeft").getValue()));

                        new LogThis(1, "Finished reading in variables");

                        Budget budget = new Budget(budgetName, budgetAmount);
                        budget.setBalanceLeft(balanceLeft);
                        adapter.add(budget);
                    } catch (Exception e) {
                        e.printStackTrace();
                        new ToastMessages(BudgetPanel.this, "Error retrieving budget information");
                    }
                }
                updateTotal();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // onClick method for ListView items
        budgetList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String name = adapter.getItem(i).getBudgetName();
                        Intent intent = new Intent(BudgetPanel.this, SingleBudgetDisplay.class);
                        intent.putExtra("budgetName", name);
                        startActivity(intent);
                    }
                }
        );
    }

    // function to update total balance left
    private void updateTotal() {
        Double sumOfAllAccounts = 0.00;
        for (Budget budget : adapter.getBudgets()) {
            sumOfAllAccounts += budget.getBalanceLeft();
            Log.d("DataCapture", "getting amounts");
        }

        String total;
        if (sumOfAllAccounts < 0) {
            total = String.format(Locale.US, "$(%.2f)", -sumOfAllAccounts);
        } else {
            total = String.format(Locale.US, "$%.2f", sumOfAllAccounts);
        }

        totalBalanceLeft.setText(total);
    }

    // function call to create a new budget
    public void createNewBudget(View view) {
        Intent intent = new Intent(this, CreateNewBudget.class);
        startActivity(intent);
    }

    // resets all budgets to their budget amount
    public void resetBudgets(View view) {
        for (Budget budget : adapter.getBudgets()) {
            budget.resetBudget();
            reference.child(budget.getBudgetName()).setValue(budget);
        }

        new ToastMessages(this, "Reset all Budgets");
    }

    // setUp for menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bp_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
