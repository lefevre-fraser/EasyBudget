package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;

// popup window type in order to add expenses to a budget
public class ManuallyAddBudgetHistory extends Activity {
    private ActionBar actionBar;
    private EditText amount;
    private EditText reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manually_add_budget_history);

        // set up display of popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .4);

        getWindow().setLayout(width, height);

        actionBar = getActionBar();
        actionBar.hide();

        amount = (EditText) findViewById(R.id.MADH_amount);
        reason = (EditText) findViewById(R.id.MADH_reason);
    }

    // save added expense to the budget
    public void MADH_onSaveButtonClick(View view) {
        Double amountDouble = Double.valueOf(String.valueOf(amount.getText()));
        String reasonString = String.valueOf(reason.getText());
        if (amountDouble == null) {
            new ToastMessages(this, "must enter a value");
            return;
        } else if (amountDouble.equals(0.00)) {
            new ToastMessages(this, "must enter a value greater than $0.00");
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("amount", amountDouble);
        intent.putExtra("reason", reasonString);
        setResult(RESULT_OK, intent);
        finish();
    }

    // cancel addition of expense to budget
    public void MADH_onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
