package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

public class SimpleValueChange extends Activity {
    private ActionBar actionBar;
    private EditText amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_value_change);

        // setup pop-up window display
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .4);

        getWindow().setLayout(width, height);

        actionBar = getActionBar();
        actionBar.hide();

        amount = (EditText) findViewById(R.id.SVC_valueToChange);
    }

    // save button for change of budget amount
    public void SVC_onSaveButtonClick(View view) {
        Intent intent = new Intent();

        if (String.valueOf(amount.getText()).equals("") ||
                String.valueOf(amount.getText()).equals(".")) {
            new ToastMessages(this, "Must enter a valid amount");
            return;
        } else if (Double.valueOf(String.valueOf(amount.getText())) == 0.00) {
            new ToastMessages(this, "Value must be greater than $0.00");
            return;
        }

        intent.putExtra("amount", Double.valueOf(String.valueOf(amount.getText())));
        setResult(RESULT_OK, intent);
        finish();
    }

    // cancel change of budget amount
    public void SVC_onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
