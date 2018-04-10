package com.example.peter.sugar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This Receiver fires when the user should be reminded of his closing time.
 * For now, only a simple Notification is displayed.
 *
 * TODO: Providing different types of "reminders" and the option to choose one of them.
 */
public class ClosingTimeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.LOG_TAG, "ClosingTimeReceiver: onReceive");

        String indexString = intent.getCategories().toArray(new String[0])[0];
        int index = Integer.parseInt(indexString);
        int hourOfDay = intent.getIntExtra(MainActivity.EXTRA_HOUR_OF_DAY, 0);
        int minute = intent.getIntExtra(MainActivity.EXTRA_MINUTE, 0);
        TimeObject time = new TimeObject(hourOfDay, minute);

        TimeManager timeMgr = new TimeManager(context);
        timeMgr.setNextClosingTime(index, time);

        Notification.Builder builder = new Notification.Builder(context);

        builder.setSmallIcon(R.mipmap.sugar)
                .setContentTitle(context.getString(R.string.closing_time_title))
                .setContentText(context.getString(R.string.closing_time_text))
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_VIBRATE);

        Notification noti = builder.build();
        NotificationManager notiMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notiMgr != null) {
            notiMgr.notify(index, noti);
        }
    }
}
