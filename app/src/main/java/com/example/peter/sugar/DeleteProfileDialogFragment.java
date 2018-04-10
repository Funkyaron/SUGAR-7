package com.example.peter.sugar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SHK on 15.11.17.
 */

public class DeleteProfileDialogFragment extends DialogFragment
{
    private String itemNames[];
    private ArrayList<String> selectedProfiles = new ArrayList<String>(0);

    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        setRetainInstance(true);
        itemNames = getArguments().getStringArray("profNames");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DeletionDialogTheme);
        builder.setTitle("Wählen Sie die zu löschenden Profile aus");
        builder.setMultiChoiceItems(itemNames,null,new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which,boolean isChecked)
            {
                if(isChecked)
                {
                    selectedProfiles.add(itemNames[which]);
                } else if (!isChecked && selectedProfiles.contains(itemNames[which])) {
                    selectedProfiles.remove(itemNames[which]);
                }
            }
        });
        builder.setNegativeButton("Abbrechen",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int id)
            {
                dismiss();
            }
        });
        builder.setPositiveButton("Löschen",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int id)
            {
                for( String currProfileName : selectedProfiles )
                {
                    new File(getActivity().getFilesDir()+"/"+currProfileName+".xml").delete();
                }
                ListProfilesActivity parentActivity = (ListProfilesActivity) getActivity();
                parentActivity.updateProfilesList();
            }
        });
        final AlertDialog toReturn = builder.create();
        toReturn.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog)
            {
                toReturn.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                toReturn.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        return toReturn;
    }

    @Override
    public void onDestroyView()
    {
        Dialog deleteProfilesDialog = getDialog();

        if( deleteProfilesDialog != null && getRetainInstance() )
        {
            deleteProfilesDialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
