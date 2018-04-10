package com.example.peter.sugar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This Receiver is called when a Profile should be disabled, that means calls are restricted
 * for that Profile from now on. The Profile name has to be passed as an Intent category,
 * because the Broadcast Intents must be different from each other for every Profile according
 * to filterEquals()-method. In this method, Extras are ignored.
 *
 * Does nothing if the passed Profile is not active, so make sure you set allowed = true
 * every time you deactivate a Profile.
 */

public class DisableProfileReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(MainActivity.LOG_TAG, "DisableProfileReceiver: onReceive()");

        String name = intent.getCategories().toArray(new String[0])[0];

        Profile prof = null;
        try {
            prof = Profile.readProfileFromXmlFile(name, context);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, "Error reading Profile: " + e.toString());
        }

        if(prof == null || !(prof.isActive()))
            return;

        prof.setAllowed(true);
        TimeManager mgr = new TimeManager(context);
        mgr.setNextDisable(prof);
        try {
            prof.saveProfile(context);
        } catch(Exception e) {
            Log.e(MainActivity.LOG_TAG, e.toString());
        }

        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.mipmap.sugar)
                .setContentTitle(name)
                .setContentText(context.getString(R.string.calls_allowed))
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_LOW);

        Notification noti = builder.build();

        NotificationManager notiMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notiMgr != null) {
            notiMgr.notify(name.hashCode(), noti);
        }
    }
}
