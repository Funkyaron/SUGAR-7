package com.example.peter.sugar;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class CreateProfileActivity extends ActivityContainingProfile
{
    private int selectedWeekDay;

    private EditText profileNameInput;
    private TextView[] weekdayViews;
    private CheckBox[] weekdayCheckboxes;
    private Button editStartTimeButton;
    private Button editEndTimeButton;
    private Button finishButton;
    private Button cancelButton;

    private Profile prof;
    private String name;
    private boolean[] days;
    private TimeObject[] startTimes;
    private TimeObject[] endTimes;

    @Override @NonNull
    protected Profile createProfile() {
        return new Profile();
    }


    @Override
    protected void onCreate(Bundle savedInstances)
    {
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_create_profile);

        selectedWeekDay = 0;



        // Initialize views
        profileNameInput = (EditText) findViewById(R.id.edit_profile_name);

        weekdayViews = new TextView[7];
        TableRow viewRow = (TableRow) findViewById(R.id.days_row);
        for(int currentDay = 0; currentDay < weekdayViews.length; currentDay++) {
            weekdayViews[currentDay] = (TextView) viewRow.getChildAt(currentDay);
        }

        weekdayCheckboxes = new CheckBox[7];
        TableRow checkBoxRow = (TableRow) findViewById(R.id.days_checkboxes);
        for(int currentDay = 0; currentDay < weekdayCheckboxes.length; currentDay++) {
            weekdayCheckboxes[currentDay] = (CheckBox) checkBoxRow.getChildAt(currentDay);
        }

        editStartTimeButton = (Button) findViewById(R.id.start_time_button);
        editEndTimeButton = (Button) findViewById(R.id.end_time_button);
        finishButton = (Button) findViewById(R.id.finish_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);



        // Extract Information from Profile and set up the Views
        prof = getProfile();
        name = prof.getName();
        days = prof.getDays();
        startTimes = prof.getStart();
        endTimes = prof.getEnd();

        weekdayViews[selectedWeekDay].setBackgroundResource(R.drawable.weekday_activated);
        for(int currentDay = 0; currentDay < weekdayCheckboxes.length; currentDay++) {
            weekdayCheckboxes[currentDay].setChecked(days[currentDay]);
        }
        editStartTimeButton.setText(getString(R.string.from_plus_time, startTimes[selectedWeekDay]));
        editEndTimeButton.setText(getString(R.string.to_plus_time, endTimes[selectedWeekDay]));
        if(!days[selectedWeekDay]) {
            editStartTimeButton.setVisibility(View.INVISIBLE);
            editEndTimeButton.setVisibility(View.INVISIBLE);
        }



        // Implement functionality
        /* Set OnClickListener for each TextView inside the array 'weekdayViews' */
        for( int currentWeekDay = 0; currentWeekDay < weekdayViews.length; currentWeekDay++ )
        {
            final int selectDay = currentWeekDay;
            weekdayViews[currentWeekDay].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    selectedWeekDay = selectDay;
                    for( int currentWeekDay = 0; currentWeekDay < weekdayViews.length; currentWeekDay++ ) {
                        if( currentWeekDay == selectedWeekDay ) {
                            weekdayViews[currentWeekDay].setBackgroundResource(R.drawable.weekday_activated);
                        } else {
                            weekdayViews[currentWeekDay].setBackgroundResource(R.drawable.weekday_deactivated);
                        }
                    }
                    editStartTimeButton.setText(getString(R.string.from_plus_time, startTimes[selectedWeekDay]));
                    editEndTimeButton.setText(getString(R.string.to_plus_time, endTimes[selectedWeekDay]));
                    if(days[selectedWeekDay]) {
                        editStartTimeButton.setVisibility(View.VISIBLE);
                        editEndTimeButton.setVisibility(View.VISIBLE);
                    } else {
                        editStartTimeButton.setVisibility(View.INVISIBLE);
                        editEndTimeButton.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        // Set OnCLickListener for every checkbox.
        for( int currentWeekDay = 0; currentWeekDay < weekdayCheckboxes.length; currentWeekDay++ )
        {
            final int selectDay = currentWeekDay;
            weekdayCheckboxes[currentWeekDay].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton selectedCheckbox,boolean isChecked)
                {
                    if(isChecked)
                    {
                        prof.setDayActiveForWeekDay(selectDay);
                        if( selectDay == selectedWeekDay )
                        {
                            editStartTimeButton.setVisibility(View.VISIBLE);
                            editEndTimeButton.setVisibility(View.VISIBLE);
                            editStartTimeButton.setText(CreateProfileActivity.this.getString(R.string.from_plus_time,startTimes[selectDay]));
                            editEndTimeButton.setText(CreateProfileActivity.this.getString(R.string.to_plus_time,endTimes[selectDay]));
                        }
                    } else{
                        prof.setDayInactiveForWeekDay(selectDay);
                        if( selectDay == selectedWeekDay ) {
                            editStartTimeButton.setVisibility(View.INVISIBLE);
                            editEndTimeButton.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }

        // Set OnClickListeners for the other buttons.
        editStartTimeButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               pickTime(true);
           }
        });
        editEndTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                pickTime(false);
            }
        });
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                name = profileNameInput.getText().toString();
                if( name.isEmpty() ) {
                    Toast.makeText(CreateProfileActivity.this, R.string.prompt_enter_profile_name, Toast.LENGTH_LONG).show();
                    return;
                }
                prof.setName(name);
                try {
                    prof.saveProfile(getApplicationContext());
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                Log.d(MainActivity.LOG_TAG,"File exists" + new File(getApplicationContext().getFilesDir()+"/"+name+".xml").exists());
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedWeekDay",selectedWeekDay);
        outState.putString("name",profileNameInput.getText().toString());
        for( int currDay = 0; currDay < days.length; currDay++ )
        {
            outState.putBoolean("day"+currDay,days[currDay]);
        }
        for( int currDay = 0; currDay < startTimes.length; currDay++ )
        {
            outState.putString("start"+currDay,startTimes[currDay].toString());
        }
        for( int currDay = 0; currDay < startTimes.length; currDay++ )
        {
            outState.putString("end"+currDay,endTimes[currDay].toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        selectedWeekDay = savedInstanceState.getInt("selectedWeekDay");
        name = savedInstanceState.getString("name");
        Log.d(MainActivity.LOG_TAG,"Content of 'name' :"+name);
        for( int currDay = 0; currDay < days.length; currDay++ )
        {
            days[currDay] = savedInstanceState.getBoolean("day"+currDay);
        }
        for( int currDay = 0; currDay < startTimes.length; currDay++ )
        {
            startTimes[currDay] = new TimeObject(savedInstanceState.getString("start"+currDay));
        }
        for( int currDay = 0; currDay < endTimes.length; currDay++ ) {
            endTimes[currDay] = new TimeObject(savedInstanceState.getString("end" + currDay));
        }
        for( int currDay = 0; currDay < weekdayViews.length; currDay++ )
        {
            weekdayCheckboxes[currDay].setChecked(days[currDay]);
            if( currDay == selectedWeekDay )
            {
                weekdayViews[currDay].setBackgroundResource(R.drawable.weekday_activated);
            } else if ( currDay != selectedWeekDay ) {
                weekdayViews[currDay].setBackgroundResource(R.drawable.weekday_deactivated);
            }
        }
    }

    private void pickTime(boolean isStart)
    {
        DialogFragment timePickerFragment = new TimePickerFragment();
        Bundle timePickerFragmentBundle = new Bundle();
        timePickerFragmentBundle.putInt(MainActivity.EXTRA_INDEX, selectedWeekDay);
        timePickerFragmentBundle.putBoolean(MainActivity.EXTRA_IS_START, isStart);
        timePickerFragment.setArguments(timePickerFragmentBundle);
        timePickerFragment.show(getFragmentManager(),"pickTime");
    }
}
