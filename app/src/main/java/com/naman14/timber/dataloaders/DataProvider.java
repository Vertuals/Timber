package com.naman14.timber.dataloaders;

import android.content.Context;

import com.naman14.timber.R;
import com.naman14.timber.activities.SearchActivity;
import com.naman14.timber.models.Album;
import com.naman14.timber.models.Artist;
import com.naman14.timber.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DataProvider {


    public static Observable<List<Object>> searchLocalSongs(final Context ctx, final String query) {
        return Observable.fromCallable(new Callable<List<Object>>() {
            @Override
            public List<Object> call() throws Exception {
                ArrayList<Object> results = new ArrayList<>(27);
                List<Song> songList = SongLoader.searchSongs(ctx, query, 10);
                if (!songList.isEmpty()) {
                    results.add(ctx.getString(R.string.songs));
                    results.addAll(songList);
                }


                List<Album> albumList = AlbumLoader.getAlbums(ctx, query, 7);
                if (!albumList.isEmpty()) {
                    results.add(ctx.getString(R.string.albums));
                    results.addAll(albumList);
                }

                List<Artist> artistList = ArtistLoader.getArtists(ctx, query, 7);
                if (!artistList.isEmpty()) {
                    results.add(ctx.getString(R.string.artists));
                    results.addAll(artistList);
                }

                return results;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
