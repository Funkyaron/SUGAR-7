package com.example.peter.sugar;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.service.notification.StatusBarNotification;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Service which replaces the default phone app. If an incoming phone call is
 * detected, "onScreenCall(Call.Details details)" is invoked and the CallScreeningService
 * checks whether the call is allowed or disallowed.
 */
public class CallScreeningServiceImpl extends CallScreeningService
{
    private String TAG = "CallScreeningServiceImpl";
    static boolean shouldBlockAbsolutely = false;
    private int currentRingerMode;
    private AudioManager mAudioManager;
    private PackageManager packages;
    private List<PackageInfo> installedPackages;
    private CallResponse.Builder builder;
    private CallResponse positiveResponse;
    private CallResponse negativeResponse;

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        Log.d(TAG,"Starting CallScreeningService ...");
        return START_STICKY;
    }

    /**
     * Builds the pre-defined answers to an allowed phone call and a disallowed one.
     */
    public void onCreate()
    {
        builder = new CallResponse.Builder();

        packages = getPackageManager();
        installedPackages = packages.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        Log.d(TAG,"Installed packages: " + installedPackages.toString());

        // Build positive response
        builder.setDisallowCall(false);
        builder.setRejectCall(false);
        builder.setSkipNotification(false);
        positiveResponse = builder.build();

        // Build negative response
        builder.setDisallowCall(true);
        builder.setSkipNotification(true);
        builder.setRejectCall(true);
        negativeResponse = builder.build();
    }

    /**
     * Routine which allows or disallows the incoming phone call
     * @param callDetails represent the details of the incoming call
     */
    @Override
    public void onScreenCall(Call.Details callDetails)
    {
        // Step I: Convert the incoming phone number into a human-readable number
        String callerName = callDetails.getCallerDisplayName();
        String callerNumber = callDetails.getHandle().getSchemeSpecificPart();
        // Step II: Check whether the incoming call is allowed or disallowed
        boolean shouldNumberBeBlocked = shouldBlock(callerNumber);
        Log.d(TAG, "Incoming call: [ " + callerName + "," + callerNumber + "," + " ]");

        // Step III: Determine whether phone call is answered or blocked
        if(shouldNumberBeBlocked)
        {
            Log.d(TAG,"This number should be blocked!");
            respondToCall(callDetails,negativeResponse);

            // Send SMS to caller and display toast which indicates the action
            boolean didSendMessage;
            try {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(callerNumber, null, getString(R.string.reject_message), null, null);
                didSendMessage = true;
            } catch(SecurityException e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
                didSendMessage = false;
            }
            if(didSendMessage) {
                Toast.makeText(getApplicationContext(), "SMS an Anrufer gesendet!", Toast.LENGTH_SHORT).show();
            }
        } else if (!shouldNumberBeBlocked) {
            respondToCall(callDetails,positiveResponse);
            Log.d(TAG,"This number shouldn't be blocked!");
        }
    }

    public boolean onUnbind(Intent intent)
    {
        Log.d(TAG,"Unbind function called!");
        return false;
    }

    private boolean shouldBlock(String number) {
        Log.d(TAG, "CallScreeningService: shouldBlock()");
        if(shouldBlockAbsolutely) {
            Log.d(TAG, "DoNotDisturb applies");
            return true;
        }

        Profile[] allProfiles = Profile.readAllProfiles(this);
        // This foreach-loop goes through all existing profiles. If the number matches any blocking condition,
        // the loop will be canceled and true will be returned.
        for(Profile prof : allProfiles) {
            Log.d(TAG, prof.toString());
            // Skip this profile, if it allows calls.
            if(!(prof.isAllowed())) {
                Log.d(TAG, "Profile does not allow. Checking for numbers");
                Log.d(TAG, "Mode: " + prof.getMode());
                // Go through any possible mode.
                if(prof.getMode() == Profile.MODE_BLOCK_ALL) {
                    // Always block.
                    Log.d(TAG, "Profile should block + MODE_BLOCK_ALL");
                    return true;
                }
                else if(prof.getMode() == Profile.MODE_BLOCK_SELECTED) {
                    // Block only if the given number is selected in the profile.
                    if(prof.getPhoneNumbers().contains(number) || prof.getPhoneNumbers().size() == 0) {
                        Log.d(TAG, "Profile should block + MODE_BLOCK_SELECTED");
                        return true;
                    }
                }
                else if(prof.getMode() == Profile.MODE_BLOCK_NOT_SELECTED) {
                    // Block only if the given number is not selected in the profile.
                    if(!(prof.getPhoneNumbers().contains(number)) || prof.getPhoneNumbers().size() == 0) {
                        Log.d(TAG, "Profile should block + MODE_BLOCK_NOT_SELECTED");
                        return true;
                    }
                }
            }
        }
        // If we made it until here, there is no profile that wants to block the given phone number.
        Log.d(MainActivity.LOG_TAG, "Everything's fine -> Call allowed");
        return false;
    }

}
