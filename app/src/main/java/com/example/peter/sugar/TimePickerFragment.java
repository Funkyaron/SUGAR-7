package com.example.peter.sugar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Peter on 14.07.2017.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private int index;
    private boolean isStart;

    @Override
    public Dialog onCreateDialog(Bundle savedInstance)
    {
        Bundle args = getArguments();
        index = args.getInt(MainActivity.EXTRA_INDEX);
        Log.d(MainActivity.LOG_TAG, "index is: " + index);
        isStart = args.getBoolean(MainActivity.EXTRA_IS_START);
        Log.d(MainActivity.LOG_TAG, "isStart: " + isStart);

        TimeObject time;

        ActivityContainingProfile parentActitivty = (ActivityContainingProfile) getActivity();
        Profile prof = parentActitivty.getProfile();

        if(isStart) {
            time = prof.getStart()[index];
        } else {
            time = prof.getEnd()[index];
        }
        return new TimePickerDialog(getActivity(),this,
                time.getHour(),time.getMinute(), true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        ActivityContainingProfile parentActivity = (ActivityContainingProfile) getActivity();
        Profile prof = parentActivity.getProfile();
        TimeObject modifiedTime = new TimeObject(hourOfDay, minute);

        Log.d(MainActivity.LOG_TAG, "modifiedTime at beginning: " + modifiedTime);

        // Check if start time is earlier than end time.
        // Otherwise the selection by the user is not valid and will not take effect.
        boolean isValid = true;
        TimeObject startTime;
        TimeObject endTime;
        if(isStart) {
            startTime = new TimeObject(modifiedTime);
            endTime = prof.getEnd()[index];
        } else {
            startTime = prof.getStart()[index];
            endTime = new TimeObject(modifiedTime);
        }

        TimeObject nextDayStartTime = new TimeObject(0,0);
        TimeObject previousDayEndTime = new TimeObject(0,0);
        TimeObject newNextDayStartTime = new TimeObject(0,0);
        TimeObject newPreviousDayEndTime = new TimeObject(0,0);

        if(endTime.earlierThan(startTime)) {
            nextDayStartTime = prof.getStart()[index + 1 % 7];
            isValid = endTime.earlierThan(nextDayStartTime);
            if (!isValid) {
                if (prof.getDays()[index + 1 % 7]) {
                    Toast.makeText(parentActivity, R.string.non_valid_entry, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    newNextDayStartTime = endTime;
                    newNextDayStartTime.setMinute(endTime.getMinute() + 5);
                    prof.setStartForDay(index + 1 % 7, newNextDayStartTime);
                }
            }
        }
        if(prof.getEnd()[((index - 1) % 7 + 7) % 7].earlierThan(prof.getStart()[((index - 1) % 7 + 7) % 7])) {
            previousDayEndTime = prof.getEnd()[index - 1 % 7];
            isValid = previousDayEndTime.earlierThan(startTime);
            if (!isValid) {
                if (prof.getDays()[((index - 1) % 7 + 7) % 7]) {
                    Toast.makeText(parentActivity, R.string.non_valid_entry, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    newPreviousDayEndTime = startTime;
                    newPreviousDayEndTime.setMinute(startTime.getMinute() - 5);
                    prof.setEndForDay(((index - 1) % 7 + 7) % 7, newPreviousDayEndTime);
                }
            }
        }

        Log.d(MainActivity.LOG_TAG, "modifiedTime: " + modifiedTime);
        Log.d(MainActivity.LOG_TAG, "startTime: " + startTime);
        Log.d(MainActivity.LOG_TAG, "endTime: " + endTime);
        Log.d(MainActivity.LOG_TAG, "nextDayStartTime: " + nextDayStartTime);
        Log.d(MainActivity.LOG_TAG, "previousDayEndTime: " + previousDayEndTime);
        Log.d(MainActivity.LOG_TAG, "newNextDayStartTime: " + newNextDayStartTime);
        Log.d(MainActivity.LOG_TAG, "newPreviousDayEndTime: " + newPreviousDayEndTime);

        // Modify the time of the selected day in the corresponding profile.
        if(isStart) {
            prof.setStartForDay(index, modifiedTime);
        } else {
            prof.setEndForDay(index, modifiedTime);
        }

        // Set the relevant text in the parent Activity.
        if(isStart) {
            Button startTimeButton = (Button) parentActivity.findViewById(R.id.start_time_button);
            startTimeButton.setText(parentActivity.getString(R.string.from_plus_time, modifiedTime));
        } else {
            Button endTimeButton = (Button) parentActivity.findViewById(R.id.end_time_button);
            endTimeButton.setText(parentActivity.getString(R.string.to_plus_time, modifiedTime));
        }


    }
}
