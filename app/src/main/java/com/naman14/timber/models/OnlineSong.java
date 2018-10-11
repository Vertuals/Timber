/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.naman14.timber.models;

import com.naman14.timber.freemp3.FreeMp3Client;

import org.json.JSONException;
import org.json.JSONObject;

public class OnlineSong {

    public final long owner_id;
    public final String artist;
    public final int duration;
    public final long id;
    public final String title;
    public final long date;

    public final String url;
    public final String access_key;
    public final String track_code;

    public boolean is_hq = false;

    public STATUS songStatus = STATUS.ONLINE;


    public OnlineSong() {
        this.id = -1;
        this.owner_id = -1;
        this.title = "";
        this.artist = "";
        this.duration = -1;
        this.date = System.currentTimeMillis() / 1000;
        this.url = "";
        this.access_key = "";
        this.track_code = "";
    }

    public OnlineSong(JSONObject resp) throws JSONException {

        this.id = resp.getInt("id");
        this.owner_id = resp.getInt("owner_id");
        this.artist = resp.getString("artist");
        this.title = resp.getString("title");
        this.duration = resp.getInt("duration");
        this.date = resp.getLong("date");
        this.url = resp.getString("url");
        this.is_hq = resp.getBoolean("is_hq");
        this.access_key = resp.getString("access_key");
        this.track_code = resp.getString("track_code");

        this.songStatus = FreeMp3Client.getSongStatus(this);
    }

    public String getFileName() {
        return title.replace(" ","_");
    }

   public enum STATUS {
        ONLINE,
       DOWNLOADING,
       DOWNLOADED
    }


    @Override
    public boolean equals (Object obj) {

        if(!(obj instanceof OnlineSong))
        {
           return false;
        }

        OnlineSong other = (OnlineSong) obj;

        return other.id == this.id;

    }
}
