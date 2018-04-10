package com.example.peter.sugar;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This activity displays the user when he is reminded of his closing time by the app for each day.
 * Provides the option of choosing a time or disable closing time reminder for each day separately.
 */
public class ClosingTimeDisplayActivity extends AppCompatActivity {

    /**
     * This String Array is used as tags for SharedPreferences to identify the
     * closing time for the corresponding day of week.
     */
    final String[] WEEKDAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"
    };

    private TimeObject[] closingTimes;
    private TextView[] timeViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_time_display);

        timeViews = new TextView[WEEKDAYS.length];



        // Initialize TimeObjects by extracting them from SharedPreferences. null if
        // the current day is not active.
        Log.d(MainActivity.LOG_TAG, "Before opening SharedPreferences");
        closingTimes = new TimeObject[WEEKDAYS.length];
        SharedPreferences savedTimes = getPreferences(Context.MODE_PRIVATE);
        for(int currentDay = 0; currentDay < WEEKDAYS.length; currentDay++) {
            String str = savedTimes.getString(WEEKDAYS[currentDay], "");
            if(str.equals("")) {
                closingTimes[currentDay] = null;
            } else {
                closingTimes[currentDay] = new TimeObject(str);
            }
        }

        // Initialize TextViews by extracting them from the TableLayout.
        // Set texts properly.
        TableLayout rootLayout = (TableLayout) findViewById(R.id.closing_times_view);
        for(int currentDay = 0; currentDay < timeViews.length; currentDay++) {
            TableRow row = (TableRow) rootLayout.getChildAt(currentDay);
            timeViews[currentDay] = (TextView) row.getChildAt(1);
            if(closingTimes[currentDay] == null) {
                timeViews[currentDay].setText("");
            } else {
                timeViews[currentDay].setText(closingTimes[currentDay].toString());
            }
        }


        // Implement functionality of the text views -> Enabling or disabling closing time reminder.
        for(int currentDay = 0; currentDay < timeViews.length; currentDay++) {
            final int currDay = currentDay;

            timeViews[currDay].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    if(closingTimes[currDay] != null) {
                        args.putInt(MainActivity.EXTRA_HOUR_OF_DAY, closingTimes[currDay].getHour());
                        args.putInt(MainActivity.EXTRA_MINUTE, closingTimes[currDay].getMinute());
                    }
                    args.putInt(MainActivity.EXTRA_INDEX, currDay);

                    DialogFragment newFragment = new ClosingTimePickerFragment();
                    newFragment.setArguments(args);
                    newFragment.show(getFragmentManager(), "closing" + currDay);
                }
            });
        }

        // Now set all alarms if there is one to be set.
        TimeManager mgr = new TimeManager(this);
        for(int currentDay = 0; currentDay < closingTimes.length; currentDay++) {
            if(closingTimes[currentDay] != null) {
                mgr.setNextClosingTime(currentDay, closingTimes[currentDay]);
            }
        }
    }

    public void setClosingTime(int index, TimeObject time) {
        closingTimes[index] = time;
    }

    public TextView[] getWeekDayViews() {
        return timeViews;
    }

}
