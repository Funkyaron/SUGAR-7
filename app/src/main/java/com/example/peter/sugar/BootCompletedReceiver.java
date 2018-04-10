package com.example.peter.sugar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.LOG_TAG, "BootCompletedReceiver: onReceive()");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            TimeManager mgr = new TimeManager(context);
            mgr.initProfiles();
        }
    }
}
