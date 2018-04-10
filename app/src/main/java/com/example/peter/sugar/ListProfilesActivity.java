package com.example.peter.sugar;

import android.app.DialogFragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class ListProfilesActivity extends AppCompatActivity implements DownloadProfilesListener
{
    private Profile[] profiles;
    private String[] profileNames;
    private ListView profilesList;


    @Override
    public void onDownloadFinished(Boolean successful) {
        updateProfilesList();
        if(!successful) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_profiles);

        profilesList = (ListView) findViewById(R.id.profiles_list);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfilesList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_profiles_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.addProfileItem :
            {
                Intent toAddProfilesActivity = new Intent(this,CreateProfileActivity.class);
                startActivity(toAddProfilesActivity);
                return true;
            }

            case R.id.removeProfileItem :
            {
                DialogFragment deleteDialog = new DeleteProfileDialogFragment();
                Bundle deleteDialogBundle = new Bundle();
                deleteDialogBundle.putStringArray("profNames",profileNames);
                deleteDialog.setArguments(deleteDialogBundle);
                deleteDialog.show(getFragmentManager(),"deleteSelectedProfiles");
                return true;
            }

            case R.id.downloadProfiles :
            {
                DialogFragment downloadFragment = new DownloadFragment();
                downloadFragment.show(getFragmentManager(),"downloadProfiles");
                return true;
            }

        }
        return false;
    }



    public void updateProfilesList() {
        profiles = Profile.readAllProfiles(this);
        ProfilesAdapter adapter = new ProfilesAdapter(this,profiles);
        profilesList.setAdapter(adapter);

        profileNames = new String[profiles.length];
        for( int currName = 0; currName < profiles.length; currName++ ) {
            profileNames[currName] = profiles[currName].getName();
            Log.d(MainActivity.LOG_TAG,"Current profile name : " + profileNames[currName]);
        }
    }

}
