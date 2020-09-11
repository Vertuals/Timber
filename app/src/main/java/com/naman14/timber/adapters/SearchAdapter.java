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

package com.naman14.timber.adapters;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.naman14.timber.MediaPreviewPlayer;
import com.naman14.timber.MusicPlayer;
import com.naman14.timber.R;
import com.naman14.timber.dialogs.AddPlaylistDialog;
import com.naman14.timber.freemp3.FreeMp3Client;
import com.naman14.timber.lastfmapi.LastFmClient;
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener;
import com.naman14.timber.lastfmapi.models.ArtistQuery;
import com.naman14.timber.lastfmapi.models.LastfmArtist;
import com.naman14.timber.models.Album;
import com.naman14.timber.models.Artist;
import com.naman14.timber.models.OnlineSong;
import com.naman14.timber.models.Song;
import com.naman14.timber.utils.NavigationUtils;
import com.naman14.timber.utils.TimberUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.Collections;
import java.util.List;

public class SearchAdapter extends BaseSongAdapter<SearchAdapter.ItemHolder> {

    private Activity mContext;
    private List searchResults = Collections.emptyList();

    public SearchAdapter(Activity context) {
        this.mContext = context;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                ItemHolder ml0 = new ItemHolder(v0);
                return ml0;
            case 1:
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_search, null);
                ItemHolder ml1 = new ItemHolder(v1);
                return ml1;
            case 2:
                View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, null);
                ItemHolder ml2 = new ItemHolder(v2);
                return ml2;
            case 4:
                View v4 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_online_song, null);
                ItemHolder ml4 = new ItemHolder(v4);
                return ml4;
            case 10:
                View v10 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_section_header, null);
                ItemHolder ml10 = new ItemHolder(v10);
                return ml10;
            default:
                View v3 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                ItemHolder ml3 = new ItemHolder(v3);
                return ml3;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, final int i) {
        switch (getItemViewType(i)) {
            case 0:
                Song song = (Song) searchResults.get(i);
                itemHolder.title.setText(song.title);
                itemHolder.songartist.setText(song.albumName);
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(song.albumId).toString(), itemHolder.albumArt,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(new FadeInBitmapDisplayer(400))
                                .build());
                setOnPopupMenuListener(itemHolder, i);
                break;
            case 1:
                Album album = (Album) searchResults.get(i);
                itemHolder.albumtitle.setText(album.title);
                itemHolder.albumartist.setText(album.artistName);
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(album.id).toString(), itemHolder.albumArt,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(new FadeInBitmapDisplayer(400))
                                .build());
                break;
            case 2:
                Artist artist = (Artist) searchResults.get(i);
                itemHolder.artisttitle.setText(artist.name);
                String albumNmber = TimberUtils.makeLabel(mContext, R.plurals.Nalbums, artist.albumCount);
                String songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songCount);
                itemHolder.albumsongcount.setText(TimberUtils.makeCombinedString(mContext, albumNmber, songCount));
                LastFmClient.getInstance(mContext).getArtistInfo(new ArtistQuery(artist.name), new ArtistInfoListener() {
                    @Override
                    public void artistInfoSucess(LastfmArtist artist) {
                        if (artist != null && itemHolder.artistImage != null) {
                            ImageLoader.getInstance().displayImage(artist.mArtwork.get(1).mUrl, itemHolder.artistImage,
                                    new DisplayImageOptions.Builder().cacheInMemory(true)
                                            .cacheOnDisk(true)
                                            .showImageOnFail(R.drawable.ic_empty_music2)
                                            .resetViewBeforeLoading(true)
                                            .displayer(new FadeInBitmapDisplayer(400))
                                            .build());
                        }
                    }

                    @Override
                    public void artistInfoFailed() {

                    }
                });
                break;
            case 10:
                itemHolder.sectionHeader.setText((String) searchResults.get(i));
                break;
            case 4:
                final OnlineSong onlineSong = (OnlineSong) searchResults.get(i);
                itemHolder.title.setText(onlineSong.title);
                itemHolder.songartist.setText(onlineSong.artist);
                switch (onlineSong.songStatus) {
                    case ONLINE:
                        itemHolder.bt_action.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_download));
                        itemHolder.bt_action.setVisibility(View.VISIBLE);
                        itemHolder.visualizer.setVisibility(View.GONE);
                        break;
                    case DOWNLOADING:
                        itemHolder.bt_action.setVisibility(View.VISIBLE);
                        itemHolder.visualizer.setVisibility(View.GONE);
                        itemHolder.bt_action.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_download_stop));
                        break;
                    case STREAMING:
                        itemHolder.bt_action.setVisibility(View.GONE);
                        itemHolder.visualizer.setVisibility(View.VISIBLE);
                    default:
                        itemHolder.bt_action.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_downloaded));
                }
                itemHolder.bt_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        executeSongAction(onlineSong, i);
                    }
                });

                break;
            case 3:
                break;
        }
    }

    public void executeSongAction(OnlineSong song, int pos) {
        switch (song.songStatus) {
            case ONLINE:
                FreeMp3Client.downloadSong(mContext, song);
                song.songStatus = OnlineSong.STATUS.DOWNLOADING;
                break;
            case DOWNLOADING:
                FreeMp3Client.cancelDownload(mContext, song);
                song.songStatus = OnlineSong.STATUS.ONLINE;
                break;
            default:
        }
        notifyItemChanged(pos);
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        long[] song = new long[1];
                        song[0] = ((Song) searchResults.get(position)).id;
                        switch (item.getItemId()) {
                            case R.id.popup_song_play:
                                MusicPlayer.playAll(mContext, song, 0, -1, TimberUtils.IdType.NA, false);
                                break;
                            case R.id.popup_song_play_next:
                                MusicPlayer.playNext(mContext, song, -1, TimberUtils.IdType.NA);
                                break;
                            case R.id.popup_song_goto_album:
                                NavigationUtils.navigateToAlbum(mContext, ((Song) searchResults.get(position)).albumId, null);
                                break;
                            case R.id.popup_song_goto_artist:
                                NavigationUtils.navigateToArtist(mContext, ((Song) searchResults.get(position)).artistId, null);
                                break;
                            case R.id.popup_song_addto_queue:
                                MusicPlayer.addToQueue(mContext, song, -1, TimberUtils.IdType.NA);
                                break;
                            case R.id.popup_song_addto_playlist:
                                AddPlaylistDialog.newInstance(((Song) searchResults.get(position))).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_song);
                //Hide these because they aren't implemented
                menu.getMenu().findItem(R.id.popup_song_delete).setVisible(false);
                menu.getMenu().findItem(R.id.popup_song_share).setVisible(false);
                menu.show();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (searchResults.get(position) instanceof Song)
            return 0;
        if (searchResults.get(position) instanceof Album)
            return 1;
        if (searchResults.get(position) instanceof Artist)
            return 2;
        if (searchResults.get(position) instanceof String)
            return 10;
        if (searchResults.get(position) instanceof OnlineSong)
            return 4;
        return 3;
    }

    public void updateSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, songartist, albumtitle, artisttitle, albumartist, albumsongcount, sectionHeader;
        protected ImageView albumArt, artistImage, menu;
        protected ImageButton bt_action;
        protected View visualizer;

        public ItemHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.song_title);
            this.songartist = (TextView) view.findViewById(R.id.song_artist);
            this.albumsongcount = (TextView) view.findViewById(R.id.album_song_count);
            this.artisttitle = (TextView) view.findViewById(R.id.artist_name);
            this.albumtitle = (TextView) view.findViewById(R.id.album_title);
            this.albumartist = (TextView) view.findViewById(R.id.album_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);

            this.sectionHeader = (TextView) view.findViewById(R.id.section_header);
            this.bt_action = view.findViewById(R.id.bt_action);
            this.visualizer = view.findViewById(R.id.visualizer);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

           final int pos = getAdapterPosition();

            switch (getItemViewType()) {
                case 0:
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            long[] ret = new long[1];
                            ret[0] = ((Song) searchResults.get(pos)).id;
                            playAll(mContext, ret, 0, -1, TimberUtils.IdType.NA,
                                    false, (Song) searchResults.get(pos), false);
                        }
                    }, 100);

                    break;
                case 1:
                    NavigationUtils.goToAlbum(mContext, ((Album) searchResults.get(pos)).id);
                    break;
                case 2:
                    NavigationUtils.goToArtist(mContext, ((Artist) searchResults.get(pos)).id);
                    break;
                case 3:
                    break;
                case 4:
                    if (searchResults.get(pos) instanceof OnlineSong) {
                        final OnlineSong newSong = (OnlineSong) searchResults.get(pos);

                            if(newSong.songStatus == OnlineSong.STATUS.ONLINE) {

                                final int oldPos = MediaPreviewPlayer.get().getOldPos();
                                if (oldPos > 0) {
                                    final OnlineSong oldSong = ((OnlineSong) searchResults.get(oldPos));
                                    oldSong.songStatus = OnlineSong.STATUS.ONLINE;
                                    notifyItemChanged(oldPos);
                                }

                                MediaPreviewPlayer.get().preview(newSong, pos);
                                newSong.songStatus = OnlineSong.STATUS.STREAMING;
                                notifyItemChanged(pos);
                            } else if (newSong.songStatus == OnlineSong.STATUS.STREAMING) {
                                MediaPreviewPlayer.get().pausePreview();
                                newSong.songStatus = OnlineSong.STATUS.ONLINE;
                                notifyItemChanged(pos);
                            }

                    }
                        break;
                case 10:
                    break;
            }
        }

    }
}





