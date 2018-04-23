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

import java.util.ArrayList;

/**
 * Created by Funkyaron on 09.06.2017.
 *
 * The aim of this dialog is to select the contacts / phone numbers the user wants to
 * associate with a profile.
 *
 * It must be opened by an ActivityContainingProfile implementation.
 *
 */

public class ContactsDialogFragment extends DialogFragment {

    // Data template (model class?) to hold a single contact.
    private class Contact {
        String name;
        ArrayList<String> numbers;

        Contact(String name, ArrayList<String> numbers) {
            this.name = name;
            this.numbers = numbers;
        }
    }

    private boolean[] checkedItems;

    @Override
    public void onAttach(Context context) {
        Log.d(MainActivity.LOG_TAG, "CDF: onAttach()");
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(MainActivity.LOG_TAG, "CDF: onCreateDialog()");

        ActivityContainingProfile parentActivity = (ActivityContainingProfile) getActivity();
        final Profile prof = parentActivity.getProfile();

        // Extract information from the whole database.
        final Contact[] allContacts = getAllContacts();
        CharSequence[] allNames = new CharSequence[allContacts.length];
        for(int currentContact = 0; currentContact < allContacts.length; currentContact++) {
            allNames[currentContact] = allContacts[currentContact].name;
        }

        // Get information about the selected contacts for the current profile.
        ArrayList<String> profileNames = prof.getContactNames();
        final ArrayList<Contact> selectedContacts = new ArrayList<>();

        if(savedInstanceState == null) {
            checkedItems = new boolean[allContacts.length];
            for (int currentItem = 0; currentItem < allContacts.length; currentItem++) {
                if (profileNames.contains(allContacts[currentItem].name)) {
                    checkedItems[currentItem] = true;
                    selectedContacts.add(allContacts[currentItem]);
                } else {
                    checkedItems[currentItem] = false;
                }
            }
        } else {
            checkedItems = savedInstanceState.getBooleanArray("checkedItems");
            for(int currentItem = 0; currentItem < checkedItems.length; currentItem++) {
                if(checkedItems[currentItem]) {
                    selectedContacts.add(allContacts[currentItem]);
                }
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(parentActivity);
        dialogBuilder.setTitle(R.string.prompt_select_contacts)
                .setMultiChoiceItems(allNames,
                        checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                // Here you simply have to modify the list of selected contacts depending on if
                                // the item is checked or not.
                                if(isChecked) {
                                    selectedContacts.add(allContacts[which]);
                                } else {
                                    selectedContacts.remove(allContacts[which]);
                                }
                            }
                        })
                .setPositiveButton(R.string.finish, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Create new lists from the selected contacts to update the
                        // profile's data.
                        ArrayList<String> newNames = new ArrayList<>();
                        ArrayList<String> newNumbers = new ArrayList<>();
                        for(Contact currentContact : selectedContacts) {
                            newNames.add(currentContact.name);
                            newNumbers.addAll(currentContact.numbers);
                        }
                        prof.setContactNames(newNames);
                        prof.setPhoneNumbers(newNumbers);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getDialog().cancel();
                    }
                });
        return dialogBuilder.create();

        /*
        final AlertDialog toReturn = builder.create();
        toReturn.setOnShowListener(new DialogInterface.OnShowListener() {
           @Override
           public void onShow(DialogInterface dial)
           {
               toReturn.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
               toReturn.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
           }
        });
        return toReturn;
        */
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(MainActivity.LOG_TAG, "CDF: onDismiss()");
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(MainActivity.LOG_TAG, "CDF: onCancel()");
        Toast.makeText(getActivity(), R.string.contacts_on_cancel, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBooleanArray("checkedItems", checkedItems);
    }





    private Contact[] getAllContacts() {
        Cursor dataCursor = null;
        String errorMessage = "";
        try {
            dataCursor = getActivity().getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data.DISPLAY_NAME_PRIMARY,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                            ContactsContract.Data.MIMETYPE},
                    "(" + ContactsContract.Data.MIMETYPE + " =?)",
                    new String[] {ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY
            );
        } catch(Exception e) {
            Log.e(MainActivity.LOG_TAG, e.toString());
            errorMessage = ":\n" + e.toString();
        }
        if(dataCursor == null) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.error) + errorMessage, Toast.LENGTH_LONG).show();
            return new Contact[0];
        }

        ArrayList<Contact> allContacts = new ArrayList<>();

        // The idea is to go through all rows of the data cursor and remember the
        // latest contact name. If the name of the next contact equals the name
        // of the latest contact, the contact is stored more than once in the database for some
        // reason we don't want to investigate further.
        // Then we just skip the row.

        String latestContactName = null;
        dataCursor.moveToPosition(-1);
        while(dataCursor.moveToNext()) {
            String currentContactName = dataCursor.getString(
                    dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY));
            if(latestContactName != null && latestContactName.equals(currentContactName)) {
                // Duplicate -> skip row
                continue;
            }
            String number = dataCursor.getString(
                    dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String normNumber = dataCursor.getString(
                    dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
            ArrayList<String> numbers = new ArrayList<>(2);
            if(number != null) {
                numbers.add(number);
            }
            if(normNumber != null) {
                numbers.add(normNumber);
            }
            allContacts.add(new Contact(currentContactName, numbers));
            //Update latest contact name
            latestContactName = currentContactName;
        }
        dataCursor.close();
        return allContacts.toArray(new Contact[0]);
    }
}
