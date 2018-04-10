package com.example.peter.sugar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;

/**
 * Created by Funkyaron on 04.04.2017. <p/>
 * Helper class that provides methods to handle time-dependent enabling
 * or disabling of SUGAR-Profiles.
 *
 * The general idea is to jump from one alarm to another because we can set only one alarm
 * at a time. Every time an alarm is performed, the next alarm is set.
 *
 * Note that every Intent associated with an alarm has to be unique, otherwise one alarm
 * will replace another. Extras are ignored! So we add the profile name as a category
 * and use different Receivers for enabling and disabling.
 */

public class TimeManager {

    private AlarmManager mAlarmManager;
    private Context context;

    public TimeManager(Context context) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
    }

    /**
     * Determines when the given profile should be enabled the next time and sets the
     * correspondent alarm. When a profile is enabled, calls from the associated
     * contacts are allowed.
     *
     * @param profile The profile for which the action should be performed
     */
    public void setNextEnable(Profile profile) {

        Log.d(MainActivity.LOG_TAG, "TimeManager: setNextEnable()");

        String name = profile.getName();
        boolean[] days = profile.getDays();
        TimeObject[] start = profile.getStart();

        // First check if any day of week should apply
        boolean shouldApply = false;
        for (boolean day : days)
        {
            if (day)
                shouldApply = true;
        }
        if (!shouldApply)
            return;

        // Prepare the alarm
        Intent intent = new Intent(context, EnableProfileReceiver.class);
        intent.addCategory(name);

        PendingIntent pending = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long targetTime = getTargetStartTime(days, start);

        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTime, pending);
    }


    /**
     * Determines when the given profile sould be disabled the next time and sets the
     * correspondent alarm. When a profile is disabled, calls from the associated
     * contacts are blocked.
     *
     * @param profile The profile for which the action should be performed
     */
    public void setNextDisable(Profile profile) {

        Log.d(MainActivity.LOG_TAG, "TimeManager: setNextDisable()");

        // Thins method is very similar to setNextEnable(). For comments see there.

        String name = profile.getName();
        boolean[] days = profile.getDays();
        TimeObject[] end = profile.getEnd();



        boolean shouldApply = false;
        for (boolean day : days)
        {
            if (day)
                shouldApply = true;
        }
        if (!shouldApply)
            return;

        Intent intent = new Intent(context, DisableProfileReceiver.class);
        intent.addCategory(name);

        PendingIntent pending = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long targetTime = getTargetEndTime(days, profile.getStart(), end);

        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTime, pending);
    }


    public void setNextClosingTime(int dayIndex, TimeObject time) {
        Log.d(MainActivity.LOG_TAG, "TimeManager: setNextClosingTime");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_WEEK, toCalendarDay(dayIndex));
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        long targetTime = cal.getTimeInMillis();

        if(targetTime <= currentTime) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        targetTime = cal.getTimeInMillis();

        Intent intent = new Intent(context, ClosingTimeReceiver.class);
        intent.addCategory("" + dayIndex);
        intent.putExtra(MainActivity.EXTRA_HOUR_OF_DAY, time.getHour());
        intent.putExtra(MainActivity.EXTRA_MINUTE, time.getMinute());
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent pending = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTime, pending);
    }


    public void unsetClosingTime(int index) {
        Log.d(MainActivity.LOG_TAG, "TimeManager: unsetClosingTime");

        Intent intent = new Intent(context, ClosingTimeReceiver.class);
        intent.addCategory("" + index);
        PendingIntent pending = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.cancel(pending);
    }



    /**
     * Checks if the given profile should currently be enabled or not and updates its status.
     * It also sets the next enabling and disabling alarms properly.
     *
     */
    public void initProfile(Profile prof) {
        Log.d(MainActivity.LOG_TAG, "TimeManager: initProfile");

        // At first, get the current time.
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cal.getTimeInMillis();
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);

        TimeObject[] startTimes = prof.getStart();
        TimeObject[] endTimes = prof.getEnd();
        boolean[] days = prof.getDays();

        // Now set the start and end time for the current day.
        // Later on we will check if we need it at all.
        cal.set(Calendar.HOUR_OF_DAY, startTimes[toIndex(currentDay)].getHour());
        cal.set(Calendar.MINUTE, startTimes[toIndex(currentDay)].getMinute());
        long startTimeInMillis = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, endTimes[toIndex(currentDay)].getHour());
        cal.set(Calendar.MINUTE, endTimes[toIndex(currentDay)].getMinute());
        long endTimeInMillis = cal.getTimeInMillis();

        int previousDayIndex = ((toIndex(currentDay) -1) % 7 + 7) % 7;
        cal.set(Calendar.HOUR, endTimes[previousDayIndex].getHour());
        cal.set(Calendar.MINUTE, endTimes[previousDayIndex].getMinute());
        long previousDayEndTime = cal.getTimeInMillis();

        boolean shouldAllow;
        if(days[previousDayIndex] && endTimes[previousDayIndex].earlierThan(startTimes[previousDayIndex]) && currentTime < previousDayEndTime) {
            shouldAllow = false;
        } else if(endTimes[toIndex(currentDay)].earlierThan(startTimes[toIndex(currentDay)])) {
            if(currentTime < startTimeInMillis) {
                shouldAllow = true;
            } else {
                shouldAllow = false;
            }
        } else {
            if(startTimeInMillis < currentTime && currentTime < endTimeInMillis) {
                shouldAllow = false;
            } else {
                shouldAllow = true;
            }
        }

        if (!shouldAllow)
        {
            prof.setAllowed(false);
            try {
                prof.saveProfile(context);
            } catch(Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }

            setNextDisable(prof);
            setNextEnable(prof);

            // Inform the user about what happened.
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.sugar)
                    .setContentTitle(prof.getName())
                    .setContentText(context.getString(R.string.calls_forbidden))
                    .setWhen(System.currentTimeMillis());

            Notification noti = builder.build();

            NotificationManager notiMgr = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notiMgr != null) {
                notiMgr.notify(prof.getName().hashCode(), noti);
            }
        } else {
            prof.setAllowed(true);
            try {
                prof.saveProfile(context);
            } catch(Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }

            setNextEnable(prof);
            setNextDisable(prof);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.sugar)
                    .setContentTitle(prof.getName())
                    .setContentText(context.getString(R.string.calls_allowed))
                    .setWhen(System.currentTimeMillis());

            Notification noti = builder.build();

            NotificationManager notiMgr = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notiMgr != null) {
                notiMgr.notify(prof.getName().hashCode(), noti);
            }
        }
    }

    public void initProfiles() {

        Log.d(MainActivity.LOG_TAG, "TimeManager: initProfiles");

        Profile[] allProfiles = Profile.readAllProfiles(context);

        // At first, get the current time.
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cal.getTimeInMillis();
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);

        // Now we collect every enabled and disabled profiles.
        ArrayList<Profile> enabledProfiles = new ArrayList<>(0);
        ArrayList<Profile> disabledProfiles = new ArrayList<>(0);

        for(Profile prof : allProfiles) {
            TimeObject[] startTimes = prof.getStart();
            TimeObject[] endTimes = prof.getEnd();
            boolean[] days = prof.getDays();

            // Now set the start and end time for the current day.
            // Later on we will check if we need it at all.
            cal.set(Calendar.HOUR_OF_DAY, startTimes[toIndex(currentDay)].getHour());
            cal.set(Calendar.MINUTE, startTimes[toIndex(currentDay)].getMinute());
            long startTimeInMillis = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, endTimes[toIndex(currentDay)].getHour());
            cal.set(Calendar.MINUTE, endTimes[toIndex(currentDay)].getMinute());
            long endTimeInMillis = cal.getTimeInMillis();

            // Now we check for the current day and time.
            if (days[toIndex(currentDay)] == true
                    && currentTime > startTimeInMillis
                    && endTimeInMillis > currentTime)
            {
                disabledProfiles.add(prof);
            } else {
                enabledProfiles.add(prof);
            }
        }



        int id = 1;

        for(Profile disProf : disabledProfiles) {
            disProf.setAllowed(false);
            try {
                disProf.saveProfile(context);
            } catch(Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }

            setNextDisable(disProf);
            setNextEnable(disProf);

            // Inform the user about what happened.
            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.sugar)
                    .setContentTitle(disProf.getName())
                    .setContentText(context.getString(R.string.calls_forbidden))
                    .setWhen(System.currentTimeMillis());

            Notification noti = builder.build();

            NotificationManager notiMgr = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notiMgr.notify(id++, noti);
        }

        for(Profile enProf : enabledProfiles) {
            enProf.setAllowed(true);
            try {
                enProf.saveProfile(context);
            } catch(Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }

            setNextEnable(enProf);
            setNextDisable(enProf);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.mipmap.sugar)
                    .setContentTitle(enProf.getName())
                    .setContentText(context.getString(R.string.calls_allowed))
                    .setWhen(System.currentTimeMillis());

            Notification noti = builder.build();

            NotificationManager notiMgr = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notiMgr.notify(id++, noti);
        }
    }



    private long getTargetStartTime(boolean[] days, TimeObject[] times)
    {
        // Figure out when to execute the alarm

        // First we get the current time
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cal.getTimeInMillis();
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);
        int currentDayIndex = toIndex(currentDay);

        cal.set(Calendar.SECOND, 0);

        // First we have to check the start time from "today".
        cal.set(Calendar.HOUR_OF_DAY, times[currentDayIndex].getHour());
        cal.set(Calendar.MINUTE, times[currentDayIndex].getMinute());
        long targetTime = cal.getTimeInMillis();
        if (days[currentDayIndex] && (currentTime < targetTime)) {
            return targetTime;
        }

        // Now we have to match the next day to apply with the corresponding start time,
        // so we have to find out the appropriate Array index.
        // First we find out how many days we have to add to the current day.

        int daysToAdd = 0;
        do {
            daysToAdd++;
            currentDayIndex = (currentDayIndex + 1) % 7;
            if(daysToAdd > 6) {
                break;
            }
        } while(!days[currentDayIndex]);

        // Now we determine the target time by adding the days to the Calendar instance
        // and extracting the right start time from the TimeObject Array.

        cal.set(Calendar.HOUR_OF_DAY, times[currentDayIndex].getHour());
        cal.set(Calendar.MINUTE, times[currentDayIndex].getMinute());
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        targetTime = cal.getTimeInMillis();

        return targetTime;
    }

    private long getTargetEndTime(boolean[] days, TimeObject[] startTimes, TimeObject[] endTimes) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        long currentTime = cal.getTimeInMillis();
        int currentDay = cal.get(Calendar.DAY_OF_WEEK);
        int currentDayIndex = toIndex(currentDay);

        cal.set(Calendar.SECOND, 0);

        int previousDayIndex = ((toIndex(currentDay) - 1) % 7 + 7) % 7;
        if (days[previousDayIndex] && endTimes[previousDayIndex].earlierThan(startTimes[previousDayIndex])) {
            cal.set(Calendar.HOUR, endTimes[previousDayIndex].getHour());
            cal.set(Calendar.MINUTE, endTimes[previousDayIndex].getMinute());
            long previousDayEndTime = cal.getTimeInMillis();

            if(currentTime < previousDayEndTime) {
                return previousDayEndTime;
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, endTimes[currentDayIndex].getHour());
        cal.set(Calendar.MINUTE, endTimes[currentDayIndex].getMinute());
        long targetTime = cal.getTimeInMillis();
        if(days[currentDayIndex]) {
            if(startTimes[currentDayIndex].earlierThan(endTimes[currentDayIndex]) && currentTime < targetTime) {
                return targetTime;
            }
            if(endTimes[currentDayIndex].earlierThan(startTimes[currentDayIndex])) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                targetTime = cal.getTimeInMillis();
                return targetTime;
            }
        }

        int daysToAdd = 0;
        do {
            daysToAdd++;
            currentDayIndex = (currentDayIndex + 1) % 7;
            if(daysToAdd > 6) {
                break;
            }
        } while(!days[currentDayIndex]);

        if(endTimes[currentDayIndex].earlierThan(startTimes[currentDayIndex]))  {
            daysToAdd += 1;
        }

        cal.set(Calendar.HOUR_OF_DAY, endTimes[currentDayIndex].getHour());
        cal.set(Calendar.MINUTE, endTimes[currentDayIndex].getMinute());
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        targetTime = cal.getTimeInMillis();

        return targetTime;
    }



    /**
     * Converts constant field values from java.util.Calendar to array-index,
     * beginning from monday.
     *
     * @param calendarDay Constant field value from java.util.Calendar
     * @return Index that can be used for an array, beginning from monday
     */
    public static int toIndex(int calendarDay) {
        switch(calendarDay) {
            case Calendar.MONDAY:
                return 0;
            case Calendar.TUESDAY:
                return 1;
            case Calendar.WEDNESDAY:
                return 2;
            case Calendar.THURSDAY:
                return 3;
            case Calendar.FRIDAY:
                return 4;
            case Calendar.SATURDAY:
                return 5;
            case Calendar.SUNDAY:
                return 6;
            default:
                return 0; // Never used.
        }
    }

    public static int toCalendarDay(int index) {
        switch(index) {
            case 0:
                return Calendar.MONDAY;
            case 1:
                return Calendar.TUESDAY;
            case 2:
                return Calendar.WEDNESDAY;
            case 3:
                return Calendar.THURSDAY;
            case 4:
                return Calendar.FRIDAY;
            case 5:
                return Calendar.SATURDAY;
            case 6:
                return Calendar.SUNDAY;
            default:
                return 0;
        }
    }
}
