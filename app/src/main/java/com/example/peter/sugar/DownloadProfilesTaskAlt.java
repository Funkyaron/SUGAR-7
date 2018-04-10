package com.example.peter.sugar;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.net.ftp.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by aron on 30.05.2017.
 */

class DownloadProfilesTaskAlt extends AsyncTask<String,Void,Boolean>
{

    Context context;
    private DownloadProfilesListener listener;
    private FTPClient androidClient = new FTPClient();

    public DownloadProfilesTaskAlt(Activity context)
    {
        this.context = context;
        listener = (DownloadProfilesListener) context;
    }

    protected Boolean doInBackground(String... params)
    {
        Log.d(MainActivity.LOG_TAG, "DownloadProfilesTask: doInBackground()");
        boolean isSuccessful = false;
        String serverName = params[0];
        int serverPort = Integer.parseInt(params[1]);
        String serverUser = params[2];
        String serverPassword = params[3];
        try {
            androidClient.connect(serverName, serverPort);
            androidClient.login(serverUser, serverPassword);
            androidClient.enterLocalPassiveMode();
            androidClient.setFileType(FTP.BINARY_FILE_TYPE);
            androidClient.changeWorkingDirectory("SUGAR");

            FTPFile[] serverFiles = androidClient.listFiles();
            File[] localFiles = context.getFilesDir().listFiles();

            Log.d(MainActivity.LOG_TAG, "Server files count: " + serverFiles.length);
            Log.d(MainActivity.LOG_TAG, "Local files count: " + localFiles.length);


            for(FTPFile currentServerFile : serverFiles)
            {
                String currentFileName = currentServerFile.getName();
                if(currentFileName.equals("..")) {
                    continue;
                }

                File localFile = new File(context.getFilesDir(), currentFileName);

                if(!localFile.exists()) {
                    Log.d(MainActivity.LOG_TAG, "Retrieving file from server");
                    FileOutputStream fos = context.openFileOutput(currentFileName, Context.MODE_PRIVATE);
                    isSuccessful = androidClient.retrieveFile(currentFileName, fos);
                    fos.close();
                }

            }
            return true;
        } catch ( IOException exception ) {
            Log.e(MainActivity.LOG_TAG, exception.toString());
            return false;
        } finally {
            try {
                androidClient.logout();
            } catch ( IOException exception ) {
                Log.e(MainActivity.LOG_TAG,exception.toString());
            }
            try {
                androidClient.disconnect();
            } catch(IOException e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }
        }
    }


    protected void onPostExecute(Boolean isSuccessful) {
        Log.d(MainActivity.LOG_TAG, "Download Successful: " + isSuccessful);
        listener.onDownloadFinished(isSuccessful);
    }
}