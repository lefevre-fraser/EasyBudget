package com.budget.lefevre.easybudget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

public class ConfirmationPopUp extends Activity {
    private TextView questionView;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_pop_up);

        // set up dimensions of popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .4);

        getWindow().setLayout(width, height);

        questionView = (TextView) findViewById(R.id.CPU_display);
        String question = "Are you sure you want to ";

        actionBar = getActionBar();
        actionBar.hide();

        // display question
        Intent intent = getIntent();
        if (intent.hasExtra("confirm/Deny")) {
            question += intent.getStringExtra("confirm/Deny");
        } else {
            question += "unknown action";
        }
        question += "?";

        questionView.setText(question);
    }

    // say yes to question
    public void confirmAction(View view) {
        setResult(RESULT_OK);
        finish();
    }

    // say no to question
    public void denyAction(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
