package com.naman14.timber.freemp3;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.naman14.timber.models.OnlineSong;

public class DownloadService extends IntentService {


    public DownloadService(String name) {
        super(name);
    }

    public static void DownloadSong(Context ctx, OnlineSong song) {

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
