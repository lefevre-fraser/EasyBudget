package com.budget.lefevre.easybudget;

import android.util.Log;

// clas to provide easy method of logging certain types of things multiple times
public class LogThis {
    private static final String DEBUG_TAG1 = "DataCapture";
    private static final String DEBUG_TAG2 = "DataWriting";
    private static final String DEBUG_TAG3 = "SomethingElse";
    private static final String DEBUG_TAG4 = "RandomMessage";

    // constructor for determining the type of log to make
    LogThis(int type, String message) {
        switch (type) {
            case 1:
                Log.d(DEBUG_TAG1, message);
                break;
            case 2:
                Log.d(DEBUG_TAG2, message);
                break;
            case 3:
                Log.d(DEBUG_TAG3, message);
                break;
            case 4: {}
            default:
                Log.d(DEBUG_TAG4, "Hi, how are you doing today?? Isn't it a nice day!?");
                break;
        }
    }
}
