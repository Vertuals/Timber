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

package com.naman14.timber.lastfmapi;

import android.content.Context;

import com.naman14.timber.utils.PreferencesUtility;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestServiceFactory {
    private static final long CACHE_SIZE = 1024 * 1024;

    public static <T> T createStatic(final Context context, String baseUrl, Class<T> clazz) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    PreferencesUtility prefs = PreferencesUtility.getInstance(context);
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request newRequest;

                        newRequest = request.newBuilder()
                                .addHeader("Cache-Control",
                                String.format("max-age=%d,%smax-stale=%d",
                                        Integer.valueOf(60 * 60 * 24 * 7),
                                        prefs.loadArtistAndAlbumImages() ? "" : "only-if-cached,", Integer.valueOf(31536000)))
                                .addHeader("Connection", "keep-alive")
                                .build();
                        return chain.proceed(newRequest);
                    }
                }).cache(new Cache(context.getApplicationContext().getCacheDir(),
                        CACHE_SIZE))
                .connectTimeout(40, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit
                .create(clazz);

    }

    public static <T> T create(final Context context, String baseUrl, Class<T> clazz) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)

                .client(new OkHttpClient.Builder()
                        .connectTimeout(40, TimeUnit.SECONDS)
                        .cache(new Cache(context.getApplicationContext().getCacheDir(),
                                CACHE_SIZE))
                        .build())
                .build();

        return retrofit
                .create(clazz);

    }


}
