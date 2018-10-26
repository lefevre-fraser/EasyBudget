package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// main activity - provides logo on startup
public class MainActivity extends AppCompatActivity {
    private static final int EXIT_ACTIVITY = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // display startup screen before launching into the rest of the activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, AccountsDisplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, EXIT_ACTIVITY);
            }
        }, 5000);
    }

    // handle result of exit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == EXIT_ACTIVITY) {
            setResult(EXIT_ACTIVITY);
            finish();
        }
    }
}