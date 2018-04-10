package com.example.peter.sugar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by arons on 22.08.2017.
 *
 * Used to display Profile names with additional icons.
 */

public class ProfilesAdapter extends ArrayAdapter<Profile> {

    private Context context;

    public ProfilesAdapter(Context context, Profile[] profiles) {
        super(context, 0, profiles);
        this.context = context;
    }

    @Override @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        View result = convertView;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        result = inflater.inflate(R.layout.profile_list_item, null);

        final Profile prof = getItem(position);
        final String name = prof.getName();

        final TextView profileNameView = (TextView) result.findViewById(R.id.profile_name);
        final TextView allowedView = (TextView) result.findViewById(R.id.profile_start);
        final ImageView profileImage = (ImageView) result.findViewById(R.id.profile_icon);
        if( prof.isActive() ) {
            profileImage.setImageResource(R.mipmap.power_on);
        } else {
            profileImage.setImageResource(R.mipmap.power_off);
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if( prof.isActive())
                {
                    profileImage.setImageResource(R.mipmap.power_off);
                    prof.setActive(false);
                    try {
                        prof.saveProfile(context);
                    } catch ( Exception e) {
                        e.printStackTrace();
                    }
                } else if ( !prof.isActive() ) {
                    profileImage.setImageResource(R.mipmap.power_on);
                    prof.setActive(true);
                    try {
                        prof.saveProfile(context);
                        TimeManager mgr = new TimeManager(context);
                        mgr.initProfile(prof);
                    } catch ( Exception e) {
                        e.printStackTrace();
                    }
                }
                if(prof.isAllowed()) {
                    allowedView.setText(R.string.calls_allowed);
                } else {
                    allowedView.setText(R.string.calls_forbidden);
                }
            }
        });

        profileNameView.setText(name);
        if(prof.isAllowed()) {
            allowedView.setText(R.string.calls_allowed);
        } else {
            allowedView.setText(R.string.calls_forbidden);
        }
        RelativeLayout clickableView = (RelativeLayout) result.findViewById(R.id.textArea);
        clickableView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent moveToEditProfileActivity = new Intent(context,EditProfileActivity.class);
                moveToEditProfileActivity.putExtra(MainActivity.EXTRA_PROFILE_NAME,name);
                context.startActivity(moveToEditProfileActivity);
            }
        });
        return result;
    }
}
