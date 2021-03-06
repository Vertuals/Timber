package com.naman14.timber.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.naman14.timber.freemp3.FreeMp3Client;

public abstract class DownloadReceiverActivity extends BaseActivity {


    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(onComplete);
    }

    BroadcastReceiver onComplete= new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                FreeMp3Client.HandleDownloadCompleteEvent(ctxt, intent);
                onSongDownloadComplete();
            }
        }
    };

    public abstract void onSongDownloadComplete();
}
