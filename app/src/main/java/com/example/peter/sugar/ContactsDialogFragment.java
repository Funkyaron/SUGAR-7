package com.example.peter.sugar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Funkyaron on 09.06.2017.
 *
 * The aim of this dialog is to select the contacts / phone numbers the user wants to
 * associate with a profile.
 *
 * It must be opened by the EditProfilesActivity
 *
 */

public class ContactsDialogFragment extends DialogFragment {

    private ArrayList<Long> mRawContactIds;
    private Cursor mDataCursor;
    private Cursor mRawCursor;

    @Override
    public void onAttach(Context context) {
        Log.d(MainActivity.LOG_TAG, "CDF: onAttach()");
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(MainActivity.LOG_TAG, "CDF: onCreateDialog()");

        ActivityContainingProfile parentActivity = (ActivityContainingProfile) getActivity();
        Profile prof = parentActivity.getProfile();

        ArrayList<String> numbers = prof.getPhoneNumbers();

        // For information about the cursors see below.
        // The RawContacts query is the one that is displayed to the user.
        try {
            mDataCursor = getDataCursor();
            mRawCursor = getRawCursor();
        } catch(SecurityException e) {
            Log.e(MainActivity.LOG_TAG, e.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.error_message_contacts_permission);
            builder.setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }

        // Now we have to reverse-engine the checked items, so we extract the RawContact IDs
        // from the data table using the list of numbers. Then we go through the RawContacts
        // query to identify the checked items using the IDs.
        // Alternatively extract the ids from savedInstanceState (see onSaveInstanceState() below).
        if(savedInstanceState == null) {
            mRawContactIds = getIdsByNumbers(numbers, mDataCursor);
        } else {
            long[] ids = savedInstanceState.getLongArray("ids");
            mRawContactIds = new ArrayList<>(ids.length);
            for(Long id : ids) {
                mRawContactIds.add(id);
            }
        }
        boolean[] checkedItems = getCheckedItemsByIds(mRawContactIds, mRawCursor);

        // Here we get the contact names which are displayed in the list out of the
        // RawContacts table.
        CharSequence[] names = getNames(mRawCursor);

        // The contacts appear twice in the Data table but once in the RawContacts table,
        // so we need to fetch the selected IDs again, this time from the RawContacts table.
        mRawContactIds = getIdsByCheckedItems(checkedItems, mRawCursor);

        // Finally, we can actually build the dialog.
        // Every time a contact is checked or unchecked we modify the list of RawContact IDs.
        // When the user is finished, we look up the phone numbers in the Data table and pass
        // them back to the parent activity by invoking the onContactsSelected()-method.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.prompt_select_contacts)
               .setMultiChoiceItems(names,
                       checkedItems,
                       new DialogInterface.OnMultiChoiceClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which,
                                               boolean isChecked) {
                               mRawCursor.moveToPosition(which);
                               if(isChecked) {
                                   Long addedId = mRawCursor.getLong(
                                           mRawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                                   mRawContactIds.add(addedId);
                               } else {
                                   Long removedId = mRawCursor.getLong(
                                           mRawCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                                   mRawContactIds.remove(removedId);
                               }
                           }
                       })
               .setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       ArrayList<String> newNumbers = getNumbersByIds(mRawContactIds, mDataCursor);
                       ArrayList<String> newNames = getNamesByIds(mRawContactIds, mRawCursor);
                       onContactsSelected(newNumbers, newNames);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       getDialog().cancel();
                   }
               });
        final AlertDialog toReturn = builder.create();
        toReturn.setOnShowListener(new DialogInterface.OnShowListener() {
           @Override
            public void onShow(DialogInterface dial)
           {
               toReturn.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
               toReturn.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
;           }
        });
        return toReturn;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(MainActivity.LOG_TAG, "CDF: onDismiss()");
        if(mDataCursor != null){
            mDataCursor.close();
        }
        if(mRawCursor != null) {
            mRawCursor.close();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(MainActivity.LOG_TAG, "CDF: onCancel()");
        Toast.makeText(getActivity(), R.string.contacts_on_cancel, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // The essential part to save is the raw contact ids, because these are the only
        // attributes changed when clicking on an item.
        // Unfortunately we have to convert from Long[] to long[]
        Long[] boxIds = mRawContactIds.toArray(new Long[0]);
        long[] ids = new long[boxIds.length];
        for(int i = 0; i < boxIds.length; i++) {
            ids[i] = boxIds[i];
        }
        outState.putLongArray("ids", ids);
    }






    /* Read the Android contacts database developer guide.
     *
     * We need to query two tables: the RawContacts table (see getRawCursor())
     * and the Data table (see getDataCursor()).
     *
     * It may be that multiple rows for the same contact appear even in the RawContacts table.
     * That is because the entries are created by different accounts. This information exists
     * only in this table, so we need the RawContacts table to differ these multiple
     * appearances of the same contact by ContactsContract.RawContacts.ACCOUNT_NAME. We
     * query for SIM1, SIM2 and Phone to get off everything that's created by WhatsApp et al.
     * Additionally, when a contact is deleted, it is NOT removed from the RawContacts table,
     * Android only sets the DELETED-Flag. So we also filter for all contacts that are not
     * marked for deletion.
     * The RawContacts query result has to be sorted by name because it is presented to the user.
     *
     * Unfortunately the phone numbers are not saved in the RawContacts table, so we need to
     * do a 2nd query for the Data table. We can use the RawContact-IDs as a bridge between
     * these two tables - they are saved in both of them.
     * We look for both the NUMBER and the NORMALIZED_NUMBER to make the block more
     * secure when reading the phone number from an incoming call.
     * We look for only those rows where there is actually a phone number (MIMETYPE).
     */

    private Cursor getDataCursor() throws SecurityException {
        Log.d(MainActivity.LOG_TAG, "CDF: getDataCursor()");

        String[] dataProjection = new String[] {
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.Data.MIMETYPE};
        String dataSelection =
                "(" + ContactsContract.Data.MIMETYPE + " =?)";
        String[] dataSelectionArgs = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

        return getActivity().getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                dataProjection,
                dataSelection,
                dataSelectionArgs,
                null);
    }

    private Cursor getRawCursor() throws SecurityException {
        Log.d(MainActivity.LOG_TAG, "CDF: getRawCursor()");

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

        return getActivity().getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                rawProjection,
                rawSelection,
                rawSelectionArgs,
                rawSortOrder);
    }







    private ArrayList<Long> getIdsByNumbers(ArrayList<String> numbers, Cursor dataCursor) {
        Log.d(MainActivity.LOG_TAG, "CDF: getIdsByNumbers()");

        ArrayList<Long> rawContactIds = new ArrayList<>(0);

        dataCursor.moveToPosition(-1);
        while(dataCursor.moveToNext()) {
            for(String currentNumber : numbers) {
                if(currentNumber.equals(
                        dataCursor.getString(dataCursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER))))
                {
                    rawContactIds.add(dataCursor.getLong(dataCursor.getColumnIndex(
                            ContactsContract.Data.RAW_CONTACT_ID)));
                    break;
                }
            }
        }
        return rawContactIds;
    }

    private ArrayList<String> getNumbersByIds(ArrayList<Long> ids, Cursor dataCursor) {
        Log.d(MainActivity.LOG_TAG, "CDF: getNumbersByIds()");

        ArrayList<String> numbers = new ArrayList<>(0);

        dataCursor.moveToPosition(-1);
        while(dataCursor.moveToNext()) {
            Long cursorId = dataCursor.getLong(dataCursor.getColumnIndex(
                    ContactsContract.Data.RAW_CONTACT_ID));
            for(Long listId : ids) {
                if(listId.equals(cursorId)) {
                    String number = dataCursor.getString(dataCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String normNumber = dataCursor.getString(dataCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                    if(number != null)
                        numbers.add(number);
                    if(normNumber != null)
                        numbers.add(normNumber);
                    break;
                }
            }
        }
        return numbers;
    }

    private boolean[] getCheckedItemsByIds(ArrayList<Long> ids, Cursor rawCursor) {
        Log.d(MainActivity.LOG_TAG, "CDF: getCheckedItemsByIds()");

        boolean[] checkedItems = new boolean[rawCursor.getCount()];
        for(int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = false;
        }

        rawCursor.moveToPosition(-1);
        while(rawCursor.moveToNext()) {
            for(Long id : ids) {
                if(id.equals(rawCursor.getLong(rawCursor.getColumnIndex(
                        ContactsContract.RawContacts._ID))))
                {
                    checkedItems[rawCursor.getPosition()] = true;
                    break;
                }
            }
        }
        return checkedItems;
    }

    private CharSequence[] getNames(Cursor rawCursor) {
        Log.d(MainActivity.LOG_TAG, "CDF: getNames()");

        String[] names = new String[rawCursor.getCount()];

        rawCursor.moveToPosition(-1);
        while(rawCursor.moveToNext()) {
            names[rawCursor.getPosition()] = rawCursor.getString(rawCursor.getColumnIndex(
                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
        }
        return names;
    }

    private ArrayList<Long> getIdsByCheckedItems(boolean[] checkedItems, Cursor rawCursor) {
        ArrayList<Long> ids = new ArrayList<>(0);
        for(int i = 0; i < checkedItems.length; i++) {
            if(checkedItems[i]) {
                rawCursor.moveToPosition(i);
                ids.add(rawCursor.getLong(rawCursor.getColumnIndex(ContactsContract.RawContacts._ID)));
            }
        }
        return ids;
    }

    private ArrayList<String> getNamesByIds(ArrayList<Long> ids, Cursor rawCursor) {
        ArrayList<String> result = new ArrayList<>();
        rawCursor.moveToPosition(-1);
        while(rawCursor.moveToNext()) {
            for(Long id : ids) {
                if(id.equals(rawCursor.getLong(rawCursor.getColumnIndex(
                        ContactsContract.RawContacts._ID))))
                {
                    result.add(rawCursor.getString(rawCursor.getColumnIndex(
                            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY)));
                    break;
                }
            }
        }
        return result;
    }

    private void onContactsSelected(ArrayList<String> numbers, ArrayList<String> names) {
        ActivityContainingProfile parentActivity = (ActivityContainingProfile) getActivity();
        Profile prof = parentActivity.getProfile();

        prof.setPhoneNumbers(numbers);
        prof.setContactNames(names);
    }
}
