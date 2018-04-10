package com.example.peter.sugar;

import java.text.DecimalFormat;

/**
 * Created by Peter on 16.05.2017.
 */

public class TimeObject {

    private int hour;
    private int minute;

    public TimeObject(int hour,int minute)
    {
        this.hour = hour;
        this.minute = minute;
    }

    // The following constructor requires a String in the format hh:mm
    public TimeObject(String time) {
        String[] timeObjectDescription = time.split(":");
        hour = Integer.parseInt(timeObjectDescription[0]);
        minute = Integer.parseInt(timeObjectDescription[1]);
    }

    public TimeObject(TimeObject to) {
        hour = to.hour;
        minute = to.minute;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public void setHour( int updatedHour )
    {
        hour = updatedHour;
    }

    public void setMinute( int updatedMinute )
    {
        if(updatedMinute >= 60) {
            hour += 1;
            minute = updatedMinute % 60;
        } else if(updatedMinute < 0) {
            hour -= 1;
            minute = (updatedMinute % 60 + 60) % 60;
        } else {
            minute = updatedMinute;
        }
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public void setTimeInMillis(long millis) {
        millis /= (1000 * 60);
        hour = (int) (millis / 60);
        minute = (int) (millis % 60);
    }

    public long getTimeInMillis() {
        return ((hour * 60) + minute) * 60 * 1000;
    }

    public String toString() {
        DecimalFormat form = new DecimalFormat("00");
        return hour + ":" + form.format(minute);
    }

    public boolean earlierThan(TimeObject other) {
        if(this.hour < other.hour) {
            return true;
        } else if(this.hour > other.hour) {
            return false;
        } else {
            return this.minute < other.minute;
        }
    }
}
