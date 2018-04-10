package com.example.peter.sugar;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by SHK on 29.03.18.
 */

public class NotificationListenerServiceImpl extends NotificationListenerService
{
    private String TAG = "NotificationObserver";

    @Override
    public void onCreate()
    {
        Log.d(TAG,"Bound to the phone!");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG,"Bound to the phone!");
        return super.onBind(intent);
    }

    @Override
    public void onListenerConnected()
    {
        Log.d(TAG,"NotificationListenerObserve");

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn)
    {
        Log.d(TAG,"Notification detected!");
        String currPackage;
        StatusBarNotification[] incomingNotifications = getActiveNotifications();
        for(int currNotification = 0; currNotification < incomingNotifications.length; currNotification++ )
        {
            currPackage = incomingNotifications[currNotification].getPackageName();
            if(currPackage.equals(getApplication().getPackageName()))
            {
            }
        }
    }

    private void fixNotificationListenerService()
    {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this,NotificationListenerService.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(this,NotificationListenerService.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }
}
