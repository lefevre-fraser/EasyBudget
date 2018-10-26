package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
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

public class AddToBudget extends Activity {
    private ActionBar actionBar;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser user;
    private String userID;

    private RadioButton radioButton;
    private Spinner budgetSpinner;

    // onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_budget);

        // set up dimensions of popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .4);

        getWindow().setLayout(width, height);

        actionBar = getActionBar();
        actionBar.hide();

        radioButton = (RadioButton) findViewById(R.id.ATB_addToBudget);
        budgetSpinner = (Spinner) findViewById(R.id.ATB_budgetSpinner);

        // check for signed in user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            new ToastMessages(this, "no user currently signed in");
            finish();
            return;
        }
        userID = user.getUid();

        // setUp listener for budgets to be edited upon desire
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(userID).child("Budgets");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> budgetNames = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            budgetNames.add(String.valueOf(ds.getKey()));
                        }
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(AddToBudget.this,
                                        R.layout.layout_simple_spinner,
                                        budgetNames);
                        budgetSpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // add transaction to budget or not
    public void ATB_onContinueButtonClick(View view) {
        if (radioButton.isChecked()) {
            Intent intent = new Intent();
            String associatedBudget = String.valueOf(budgetSpinner.getSelectedItem());
            intent.putExtra("associatedBudget", associatedBudget);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("associatedBudget", "NoAssociatedBudget");
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }
}
