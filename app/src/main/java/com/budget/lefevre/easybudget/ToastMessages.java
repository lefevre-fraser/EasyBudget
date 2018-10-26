package com.budget.lefevre.easybudget;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by mp3le on 3/31/2018.
 */

// toast messages with less effort
public class ToastMessages {

    // "default" constructor
    ToastMessages(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // constructor for long show length
    ToastMessages(Context context, String message, boolean isLong) {
        if (isLong) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
