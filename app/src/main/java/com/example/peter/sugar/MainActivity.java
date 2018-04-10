package com.example.peter.sugar;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String LOG_TAG = "SUGAR";

    public static final String EXTRA_PROFILE_NAME = "profile name";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_IS_START = "isStart";
    public static final String EXTRA_HOUR_OF_DAY = "hour";
    public static final String EXTRA_MINUTE = "minute";

    /**
     * Request code to identify the request for contacts permissions.
     */
    private final int REQUEST_CONTACTS = 1;

    /**
     * Permissions we need to read and write contacts.
     */
    private final String[] PERMISSION_CONTACTS = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,Manifest.permission.SEND_SMS};


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Concerning runtime permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(MainActivity.LOG_TAG, "ConAct: Permissions not granted, sending request.");
            ActivityCompat.requestPermissions(this, PERMISSION_CONTACTS, REQUEST_CONTACTS);
        }else {
            Log.d(LOG_TAG, "Permissions granted");
            startService(new Intent(this,NotificationListenerServiceImpl.class));
        }

        // Prompt the user to change the default dialer package to SUGAR. This is necessary to
        // block phone calls.
        Intent changeDialer = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        changeDialer.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
        startActivity(changeDialer);
    }

    // Handling runtime permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults)
    {
        Log.d(MainActivity.LOG_TAG, "ConAct: onRequestPermissionsResult()");
        if (requestCode == REQUEST_CONTACTS) {
            if(verifyPermissions(grantResults))
                Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, getString(R.string.permissions_not_granted), Toast.LENGTH_LONG).show();
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1)
            return false;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void openClosingTimeActivity(View v)
    {
        Intent moveToClosingTimeActivity = new Intent(this,ClosingTimeDisplayActivity.class);
        startActivity(moveToClosingTimeActivity);
    }

    public void openDontDisturbActivity(View v)
    {
        Intent moveToDontDisturbeActivity = new Intent(this,DoNotDisturbActivity.class);
        startActivity(moveToDontDisturbeActivity);
    }

    public void openListProfilesActivity(View v)
    {
        Intent moveToListProfilesActivity = new Intent(this,ListProfilesActivity.class);
        startActivity(moveToListProfilesActivity);
    }
}
