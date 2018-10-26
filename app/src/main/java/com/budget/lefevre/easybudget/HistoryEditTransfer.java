package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class HistoryEditTransfer extends Activity {
    private ActionBar actionBar;
    private Bundle bundle;

    private TextView from;
    private TextView to;
    private EditText amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_edit_transfer);
        // set up pop-up view
        bundle = getIntent().getExtras();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .6);

        getWindow().setLayout(width, height);

        actionBar = getActionBar();
        actionBar.hide();

        // setup ui
        from = (TextView) findViewById(R.id.HET_accountFrom);
        to = (TextView) findViewById(R.id.HET_accountTo);
        amount = (EditText) findViewById(R.id.HET_transferAmount);

        from.setText(bundle.getString("associatedAccount1"));
        to.setText(bundle.getString("associatedAccount2"));
        amount.setText(String.valueOf(bundle.getDouble("amount")));
    }

    // save method for editing a transfer item
    public void HET_onSveButtonClick(View view) {
        bundle.putDouble("amount", Double.valueOf(String.valueOf(amount.getText())));
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    // stop edit of transfer item
    public void HET_onCancelButtonClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
