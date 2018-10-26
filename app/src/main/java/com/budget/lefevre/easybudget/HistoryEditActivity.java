package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class HistoryEditActivity extends Activity {
    private ActionBar actionBar;
    private Spinner actionSpinner;
    private TextView accountName;
    private EditText amount;
    private EditText reason;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_edit);

        // setup stuff
        bundle = getIntent().getExtras();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int) (dm.widthPixels * .8);
        int height = (int) (dm.heightPixels * .6);

        getWindow().setLayout(width, height);

        actionBar = getActionBar();
        actionBar.hide();

        // set up views for use
        accountName = (TextView) findViewById(R.id.HE_accountName);
        amount = (EditText) findViewById(R.id.HE_amount);
        reason = (EditText) findViewById(R.id.HE_reason);

        // display information about history item
        accountName.setText(bundle.getString("associatedAccount1"));
        amount.setText(String.valueOf(bundle.getDouble("amount")));
        reason.setText(bundle.getString("reason"));

        // set withdraw/deposit selection to current transaction type
        String options[] = {"Withdraw", "Deposit"};
        ArrayAdapter<String> spinnerChoices = new ArrayAdapter<String>(this, R.layout.layout_simple_spinner, options);
        actionSpinner = findViewById(R.id.HE_spinner);
        actionSpinner.setAdapter(spinnerChoices);
        String type = bundle.getString("type");
        if (type != null) {
            if (type.equals("Deposit")) {
                actionSpinner.setSelection(1);
            } else {
                actionSpinner.setSelection(0);
            }
        }
    }

    // save button click method
    public void HE_onSaveButtonClick(View view) {
        Intent intent = new Intent();
        bundle.putDouble("amount", Double.valueOf(String.valueOf(amount.getText())));
        bundle.putString("reason", reason.getText().toString());
        bundle.putString("type", actionSpinner.getSelectedItem().toString());
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
        finish();
    }

    // cancel button click method
    public void HE_onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
