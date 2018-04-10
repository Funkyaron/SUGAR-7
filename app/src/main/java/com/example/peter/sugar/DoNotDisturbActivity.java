package com.example.peter.sugar;

import java.text.DecimalFormat;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.CallScreeningService;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * In this Activity, the user can define a period of time in which all calls should be blocked.
 * The period can be stopped by the user before it runs out.
 *
 * This is implemented with a CountDownTimer which keeps running, even if the Activity is destroyed,
 * so it has to be static. The Timer actually takes effect on the views, so it has to be recreated
 * every time the Activity is recreated, kind of binding to the Activity. Only if it is running.
 *
 * The state of the countdown (if the Timer is running and the elapsed time) is saved in static
 * variables.
 *
 * The blocking of every calls is implemented with a static variable in InCallServiceImpl.
 */

public class DoNotDisturbActivity extends AppCompatActivity {


    private class CustomCountDownTimer extends CountDownTimer {
        private TimeObject time;


        CustomCountDownTimer(TimeObject time) {
            super(time.getTimeInMillis(), 5000);
            millis = time.getTimeInMillis();
            this.time = time;
        }

        CustomCountDownTimer(long cMillis) {
            super(cMillis, 5000);
            millis = cMillis;
            TimeObject newTime = new TimeObject(0,0);
            newTime.setTimeInMillis(cMillis);
            time = newTime;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d(MainActivity.LOG_TAG, "CustomTimer: onTick() " + hashCode());
            time.setTimeInMillis(millisUntilFinished);
            millis = millisUntilFinished;
            Log.d(MainActivity.LOG_TAG, "millis = " + millis);
            countDownView.setText(time.toString());
        }

        @Override
        public void onFinish() {
            Log.d(MainActivity.LOG_TAG, "CustomTimer: onFinish()");
            CallScreeningServiceImpl.shouldBlockAbsolutely = false;

            setContent(false);
            isRunning = false;
        }
    }




    private TextView countDownView;
    private TextView doNotDisturbDisplay;
    private ImageButton startCountDownButton;
    private ImageButton stopCountDownButton;
    private RelativeLayout timeAmountView;
    private NumberPicker hourPicker;
    private NumberPicker minutePicker;

    private static CustomCountDownTimer timer;

    private static boolean isRunning;
    private static long millis;


    // Lifecycle Callbacks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_not_disturb);
        prepareViews();

        startCountDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallScreeningServiceImpl.shouldBlockAbsolutely = true;
                TimeObject currentTime = new TimeObject(hourPicker.getValue(), minutePicker.getValue());

                countDownView.setText(currentTime.toString());
                setContent(true);

                timer = new CustomCountDownTimer(currentTime);
                timer.start();
                isRunning = true;
            }
        });

        stopCountDownButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                timer.cancel();
                timer.onFinish();
            }
        });

        if(isRunning) {
            timer.cancel();
            setContent(true);
            TimeObject time = new TimeObject(0,0);
            time.setTimeInMillis(millis);
            countDownView.setText(time.toString());
            timer = new CustomCountDownTimer(millis);
            timer.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("hour", hourPicker.getValue());
        outState.putInt("minute", minutePicker.getValue());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        hourPicker.setValue(savedInstanceState.getInt("hour"));
        minutePicker.setValue(savedInstanceState.getInt("minute"));
    }






    // Helper methods

    private void setContent(boolean timerIsRunning) {
        if(timerIsRunning) {
            doNotDisturbDisplay.setText(getString(R.string.time_remaining));
            timeAmountView.setVisibility(View.INVISIBLE);
            countDownView.setVisibility(View.VISIBLE);
            startCountDownButton.setVisibility(View.INVISIBLE);
            stopCountDownButton.setVisibility(View.VISIBLE);

        } else {
            doNotDisturbDisplay.setText(getString(R.string.prompt_do_not_disturb));
            countDownView.setVisibility(View.INVISIBLE);
            timeAmountView.setVisibility(View.VISIBLE);
            stopCountDownButton.setVisibility(View.INVISIBLE);
            startCountDownButton.setVisibility(View.VISIBLE);
        }
    }

    private void prepareViews() {
        countDownView = (TextView) findViewById(R.id.count_down_view);
        doNotDisturbDisplay = (TextView) findViewById(R.id.do_not_disturb_display);
        startCountDownButton = (ImageButton) findViewById(R.id.start_count_down_button);
        stopCountDownButton = (ImageButton) findViewById(R.id.stop_count_down_button);
        timeAmountView = (RelativeLayout) findViewById(R.id.time_amount_view);
        hourPicker = (NumberPicker) findViewById(R.id.hour_picker);
        minutePicker = (NumberPicker) findViewById(R.id.minute_picker);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                DecimalFormat form = new DecimalFormat("00");
                return form.format(value);
            }
        });
    }
}