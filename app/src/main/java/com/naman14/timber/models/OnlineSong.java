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

import static com.naman14.timber.freemp3.FreeMp3Client.SEARCH_API_URL;

public class OnlineSong {

    private static final String STREAM_URL = SEARCH_API_URL + "?id=";

    public final long owner_id;
    public final String artist;
    public final int duration;
    public final long id;
    public final String title;
    public final long date;

    public final String url;
    public final String access_key;

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
    }

    public OnlineSong(JSONObject resp) throws JSONException {

        this.id = resp.getInt("id");
        this.owner_id = resp.getInt("owner_id");
        this.artist = resp.getString("artist");
        this.title = resp.getString("title");
        this.duration = resp.getInt("duration");
        this.date = resp.getLong("date");
        this.url = resp.getString("url");
        this.is_hq = resp.optBoolean("is_hq");
        this.access_key = resp.getString("access_key");

        this.songStatus = FreeMp3Client.getSongStatus(this);
    }

    public String getFileName() {
        return title.replace(" ","_");
    }

    public String getStreamUrl() {
        return STREAM_URL + encode(this.owner_id) + ":" + encode(this.id);
    }


    public enum STATUS {
        ONLINE,
        STREAMING,
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


    private static final char[] map = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'x', 'y', 'z', '1', '2', '3'};

    private static String encode(long input) {
        int length = map.length;
        String encoded = "";
        if (input == 0) {
            encoded = "" + map[0];
            return encoded;
        }

        if (input < 0) {
            input *= -1;
            encoded = "-";
        }

        while (input > 0) {
            long val = input % length;
            input = input / length;
            encoded += map[(int) val];
        }
        return encoded;
    }
}
