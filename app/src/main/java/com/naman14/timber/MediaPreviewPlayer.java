package com.naman14.timber;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.naman14.timber.models.OnlineSong;

import java.io.IOException;

public class MediaPreviewPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    private static MediaPreviewPlayer instance;

    private MediaPlayer mp;

    private OnlineSong currentSong;

    private boolean isPrepared;

    private int oldPos = -1;


    public MediaPreviewPlayer() {

        mp = new MediaPlayer();
       // mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
        mp.setOnErrorListener(this);

    }


    public static MediaPreviewPlayer get() {

        if(instance == null) {
            instance = new MediaPreviewPlayer();
        }
        return instance;
    }

    public void pausePreview() {
        oldPos = -1;
        if(mp.isPlaying()) {
            mp.pause();
        }
    }

    public void preview(OnlineSong song, int pos) {


        oldPos = pos;

        if(song.equals(currentSong)) {
            if(mp.isPlaying()) {
                mp.pause();
            } else if(isPrepared) {
                mp.start();
            }
                return;
        } else {
            if(mp.isPlaying()) {
                mp.stop();
            }
                mp.reset();
                isPrepared = false;
        }

        currentSong = song;

        try {
            mp.setDataSource(currentSong.getStreamUrl());
            mp.prepareAsync();
            Log.d(">>>",currentSong.getStreamUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getOldPos() {
        return oldPos;
    }


    public void stopPreview() {
        oldPos = -1;
        isPrepared = false;

        if(mp.isPlaying()) {
            mp.stop();
        }

        mp.reset();
        currentSong = null;
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(">>>", "prepared");
        isPrepared = true;
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(">>>","error:"+i+","+i1);
        return false;
    }
}
