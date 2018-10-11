package com.naman14.timber.freemp3;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.naman14.timber.dataloaders.SongLoader;
import com.naman14.timber.models.OnlineSong;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FreeMp3Client {

    private static final String SONG_EXTENSION = ".mp3";

    private static final String SEARCH_API_URL = "https://my-free-mp3s.com/";
    private static final String SEARCH_API_LINK = "api/search.php";
    private static final String STREAM_URL = "https://newtabz.stream/stream/";
    private static final String SEARCH_HEADER_KEY = "cookie";
    private static final String SEARCH_HEADER_VAL = "__cfduid=d815a6815804771ec26b49423065437ac1539088246; " +
            "_ga=GA1.2.1826088355.1539088247; _gid=GA1.2.961358189.1539088247; musicLang=en";

    private static final String MUSIC_DIR = "vplay";
    private static final String FILE_MIME = "audio/mpeg3";

    private static final char[] map = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'x', 'y', 'z', '1', '2', '3'};

    private static HashMap<Long, OnlineSong> downloadList = new HashMap<>();

    public interface SongsResponseCallBack {
        void onSongsFetched(List<OnlineSong> songs);

        void onSongsError(Throwable e);
    }


    public static void search(String query, int page, final SongsResponseCallBack callBack) {
        try {
            URL url = new URL(SEARCH_API_URL + SEARCH_API_LINK);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("q", query)
                    .addFormDataPart("p", String.valueOf(page))
                    .build();
            OkHttpClient client = new OkHttpClient();
            // GET request
            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(url)
                    .addHeader(SEARCH_HEADER_KEY, SEARCH_HEADER_VAL)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(">>>", e.getLocalizedMessage());
                    callBack.onSongsError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    callBack.onSongsFetched(getSongsList(response.body().string()));
                }
            });

        } catch (Exception e) {
            Log.e("APIManager", " Error Downloading data:" + e);
        }
    }

    private static List<OnlineSong> getSongsList(String body) {
        List<OnlineSong> onlineSongs = new ArrayList<>();

        try {
            body = body.substring(1, body.length() - 1);
            JSONArray jsonSongs = new JSONObject(body).getJSONArray("response");

            for (int i = 1; i < jsonSongs.length(); i++) {
                onlineSongs.add(new OnlineSong(jsonSongs.getJSONObject(i)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return onlineSongs;
    }

    private static String getSongUrl(long sId, long ownerId) {
        return STREAM_URL + encode(sId) + ":" + encode(ownerId);
    }

    public static void downloadSong(Context ctx, final OnlineSong song) {

        if(downloadList.containsValue(song)) {
            return;
        }

        DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(getSongUrl(song.owner_id, song.id)));
        req.setTitle(song.title);
        req.setMimeType(FILE_MIME);
        req.setDescription("Downloading ...");
        File songFile = getSongFile(song);

        if(songFile.exists()) {
            songFile.delete();
        }

        Log.i(">>>",  songFile.getAbsolutePath());

        req.setDestinationUri(Uri.fromFile(songFile));
        downloadList.put(mgr.enqueue(req), song);
    }

    public static OnlineSong.STATUS getSongStatus (OnlineSong song) {

        if (getSongDownloadQueueId(song) > 0) {
            return OnlineSong.STATUS.DOWNLOADING;
        }

        if (getSongFile(song).exists()) {
            return OnlineSong.STATUS.DOWNLOADED;
        }

        return OnlineSong.STATUS.ONLINE;
    }

    public static void cancelDownload(Context ctx, OnlineSong song) {

        long downloadId = getSongDownloadQueueId(song);

        if(downloadId <= 0) {
            return;
        }

        DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        mgr.remove(downloadId);
    }


    public static long getSongDownloadQueueId(OnlineSong song) {
        for (Map.Entry<Long, OnlineSong> entry : downloadList.entrySet()) {
            if (entry.getValue().equals(song)) {
                return entry.getKey();
            }
        }

        return -1;
    }



    public static void HandleDownloadCompleteEvent(Context ctx, Intent intent) {
        DownloadManager mgr = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = intent.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        final OnlineSong song = downloadList.get(downloadId);
        song.songStatus = OnlineSong.STATUS.DOWNLOADED;

        Cursor c = mgr.query(query);
        if (c.moveToFirst()) {
            int columnIndex = c
                    .getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL == c
                    .getInt(columnIndex)) {
                SongLoader.updateSongInfo(ctx, song);
                downloadList.remove(downloadId);
         //       ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, getSongFile(ctx, song)));
            }
        }
    }


    private static File getSongFile(OnlineSong song) {
        return new File(getMusicDirectory(), song.getFileName()
                + "_" + song.id + SONG_EXTENSION);
    }

    public static void refreshMediaLibrary(Context ctx, Uri fileUri) {
        ctx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));

    }

    private static File getMusicDirectory() {
        File musicDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), MUSIC_DIR);
        if (!musicDirectory.exists()) {
            musicDirectory.mkdir();
        }

        return musicDirectory;
    }

    private static String encode(long input) {
        int length = map.length;
        String encoded = "";
        if (input == 0) {
            encoded = "" + map[0];
            return encoded;
        }

        while (input > 0) {
            long val = input % length;
            input = input / length;
            encoded += map[(int) val];
        }
        return encoded;
    }
}
