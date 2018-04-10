package com.example.peter.sugar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by shk on 13.10.17.
 *
 * This Activity template can be used to pass a Profile to another class that needs to
 * modify it, for example from EditProfileActivity to TimePickerFragment.
 *
 * It automatically handles Profile parsingwhen entering the Activity.
 * That means you must always pass the EXTRA_PROFILE_NAME when starting an Activity
 * subclassing this one.
 */

public abstract class ActivityContainingProfile extends AppCompatActivity {
    private Profile prof;

    @NonNull
    protected abstract Profile createProfile();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prof = createProfile();
    }

    public Profile getProfile() {
        return prof;
    }
}
