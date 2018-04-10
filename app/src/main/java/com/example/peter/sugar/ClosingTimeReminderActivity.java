package com.example.peter.sugar;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * This activity realizes a type of reminder for closing times which is rather "aggressive".
 *
 * Until now (15.12.2017) it is not used.
 */
public class ClosingTimeReminderActivity extends AppCompatActivity {

    private Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closing_time_reminder);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vib != null) {
            vib.vibrate(new long[]{1000, 1000}, 0);
        }

        Button stopButton = (Button) findViewById(R.id.stop_vibrate_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(vib != null) {
                    vib.cancel();
                }
                finish();
            }
        });
    }
}
