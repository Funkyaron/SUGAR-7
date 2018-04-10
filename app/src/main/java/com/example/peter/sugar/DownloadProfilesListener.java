package com.example.peter.sugar;

/**
 * Created by shk on 18.12.17.
 *
 * Any component that uses the DownloadProfilesTask must implement this Interface. When the
 * task is finished, the onDownloadFinished() is called, so that you can react on this.
 */

public interface DownloadProfilesListener {
    void onDownloadFinished(Boolean successful);
}
