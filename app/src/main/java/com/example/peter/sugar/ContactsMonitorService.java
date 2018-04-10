package com.example.peter.sugar;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by shk on 08.12.17.
 *
 * An attempt to sync the Profiles with the contacts database. We use the JobScheduler framework
 * to update the Profiles from time to time.
 * The Profiles need to be updated when a contact that is associated with it is deleted or
 * modified.
 *
 * Unfortunately, we use minSDK 23, so we don't have the ability to react directly to
 * ContentProvider changes. Therefore we have to execute this Service periodically (see
 * MainActivity or wherever the JobScheduler is used).
 */

public class ContactsMonitorService extends JobService {

    private Handler mJobHandler;

    @Override
    public boolean onStartJob(JobParameters params) {
        mJobHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                // Here we actually update the Profiles. We use a handler so that this task is
                // not executed on the main Thread, just to be sure in case it takes too long.

                Cursor dataCursor = getDataCursor();
                Cursor rawCursor = getRawCursor();

                boolean needsReschedule = false;

                Profile[] allProfiles = Profile.readAllProfiles(ContactsMonitorService.this);

                for(Profile prof : allProfiles) {
                    Log.d(MainActivity.LOG_TAG, "Concerning Profile: " + prof.getName());

                    ArrayList<String> oldNames = prof.getContactNames();
                    ArrayList<String> oldNumbers = prof.getPhoneNumbers();

                    Log.d(MainActivity.LOG_TAG, "oldNames: " + oldNames.toString());
                    Log.d(MainActivity.LOG_TAG, "oldNumbers: " + oldNumbers.toString());

                    ArrayList<Long> selectedIds = new ArrayList<>(oldNames.size());

                    // 1. Go through the RawContacts table and check for names, add the appropriate IDs,
                    // if not already added.
                    rawCursor.moveToPosition(-1);
                    while(rawCursor.moveToNext()) {
                        String currentName = rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                        if(oldNames.contains(currentName)) {
                            Log.d(MainActivity.LOG_TAG, "Found name: " + currentName);
                            Long currentId = rawCursor.getLong(rawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                            if(!(selectedIds.contains(currentId))) {
                                selectedIds.add(currentId);
                            }
                        }
                    }

                    // 2. Go through the Data table and check for numbers, add the appropriate IDs,
                    // if not already added.
                    dataCursor.moveToPosition(-1);
                    while(dataCursor.moveToNext()) {
                        String currentNumber = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if(oldNumbers.contains(currentNumber)) {
                            Log.d(MainActivity.LOG_TAG, "Found number: " + currentNumber);
                            Long currentId = dataCursor.getLong(dataCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                            if(!(selectedIds.contains(currentId))) {
                                selectedIds.add(currentId);
                            }
                        }
                    }

                    Log.d(MainActivity.LOG_TAG, "selectedIds: " + selectedIds.toString());

                    ArrayList<String> newNames = new ArrayList<>(selectedIds.size());
                    ArrayList<String> newNumbers = new ArrayList<>(selectedIds.size());

                    // 3. Go through the RawContacts table and check for IDs, add the appropriate names.
                    rawCursor.moveToPosition(-1);
                    while(rawCursor.moveToNext()) {
                        Long currentId = rawCursor.getLong(rawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                        if(selectedIds.contains(currentId)) {
                            newNames.add(rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY)));
                        }
                    }

                    // 4. Go through the Data table, check for IDs, add the appropriate numbers.
                    dataCursor.moveToPosition(-1);
                    while(dataCursor.moveToNext()) {
                        Long currentId = dataCursor.getLong(dataCursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                        if(selectedIds.contains(currentId)) {
                            newNumbers.add(dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            newNumbers.add(dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)));
                        }
                    }

                    Log.d(MainActivity.LOG_TAG, "newNames: " + newNames.toString());
                    Log.d(MainActivity.LOG_TAG, "newNumbers: " + newNumbers.toString());

                    // Now update the profile.
                    prof.setContactNames(newNames);
                    prof.setPhoneNumbers(newNumbers);

                    try {
                        prof.saveProfile(ContactsMonitorService.this);
                    } catch(Exception e) {
                        Log.e(MainActivity.LOG_TAG, e.toString());
                        needsReschedule = true;
                    }
                }

                jobFinished((JobParameters) message.obj, needsReschedule);
                return true;
            }
        });

        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        boolean willTakeLonger = true;
        return willTakeLonger;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    private Cursor getDataCursor() {

        String[] dataProjection = new String[] {
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.Data.MIMETYPE};
        String dataSelection =
                "(" + ContactsContract.Data.MIMETYPE + " =?)";
        String[] dataSelectionArgs = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        return getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                dataProjection,
                dataSelection,
                dataSelectionArgs,
                null);
    }

    private Cursor getRawCursor() {

        String[] rawProjection = {
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.ACCOUNT_NAME,
                ContactsContract.RawContacts.DELETED};
        String rawSelection =
                "(" + ContactsContract.RawContacts.DELETED + " =?) AND (" +
                        ContactsContract.RawContacts.ACCOUNT_NAME + " !=?)";
        String[] rawSelectionArgs = {
                "0", "WhatsApp"};
        String rawSortOrder =
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY;

        return getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                rawProjection,
                rawSelection,
                rawSelectionArgs,
                rawSortOrder);
    }
}
