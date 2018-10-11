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

package com.naman14.timber.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.naman14.timber.R;
import com.naman14.timber.adapters.SearchAdapter;
import com.naman14.timber.dataloaders.AlbumLoader;
import com.naman14.timber.dataloaders.ArtistLoader;
import com.naman14.timber.dataloaders.DataProvider;
import com.naman14.timber.dataloaders.SongLoader;
import com.naman14.timber.freemp3.FreeMp3Client;
import com.naman14.timber.models.Album;
import com.naman14.timber.models.Artist;
import com.naman14.timber.models.OnlineSong;
import com.naman14.timber.models.Song;
import com.naman14.timber.provider.SearchHistory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchActivity extends DownloadReceiverActivity implements SearchView.OnQueryTextListener, View.OnTouchListener {

    @Nullable
    private Disposable mSearchTask = null;

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;

    private SearchAdapter adapter;
    private RecyclerView recyclerView;
    private View pb_loading, tv_no_result;

    private List<Object> searchResults = Collections.emptyList();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        pb_loading = findViewById(R.id.pb_loading);
        tv_no_result = findViewById(R.id.tv_no_result);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_library));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        lookup(query,true);
        hideInputManager();

        return true;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        lookup(newText,false);
        return true;
    }

    private void lookup(final String newText, boolean lookupOnline) {
        if (newText.equals(queryString) && ! lookupOnline) {
            return;
        }
        if (mSearchTask != null) {
            mSearchTask.dispose();
        }
        queryString = newText;
        if (queryString.trim().equals("")) {
            searchResults.clear();
            adapter.updateSearchResults(searchResults);
            adapter.notifyDataSetChanged();
        } else {
            if (lookupOnline) {
                setBusy(true);
                adapter.updateSearchResults(new ArrayList());
                showNoResultText(false);
                FreeMp3Client.search(queryString, 0, new FreeMp3Client.SongsResponseCallBack() {
                    @Override
                    public void onSongsFetched(final List<OnlineSong> songs) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setBusy(false);
                                showNoResultText(songs.size() == 0);
                                adapter.updateSearchResults(songs);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onSongsError(Throwable e) {

                    }
                });
            } else {
                mSearchTask = DataProvider.searchLocalSongs(getApplicationContext(),queryString)
                        .subscribeWith(new DisposableObserver<List<Object>>() {
                    @Override
                    public void onNext(List<Object> results) {
                        if (results != null) {
                            adapter.updateSearchResults(results);
                            adapter.notifyDataSetChanged();
                            showNoResultText(results.size() == 0);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }

    @Override
    protected void onDestroy() {
        if (mSearchTask != null && mSearchTask.isDisposed()){
            mSearchTask.dispose();
        }
        super.onDestroy();
    }

    public void setBusy(boolean busy) {
        pb_loading.setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    public void showNoResultText(boolean noResult) {
        tv_no_result.setVisibility(noResult ? View.VISIBLE : View.GONE);
    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();

            SearchHistory.getInstance(this).addSearchString(queryString);
        }
    }

    @Override
    public void onSongDownloadComplete() {
        //TODO: handle event in search
        adapter.notifyDataSetChanged();
    }

    private void searchLocally(String query) {



    }
}
