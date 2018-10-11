package com.naman14.timber.utils;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by Christoph Walcher on 03.12.16.
 */

public class LyricsLoader {
    private static LyricsLoader instance = null;
    private static final String BASE_API_URL = "https://makeitpersonal.co";
    private static final long CACHE_SIZE = 1024 * 1024;
    private LyricsRestService service;

    public static LyricsLoader getInstance(Context con) {
        if(instance==null)instance = new LyricsLoader(con);
        return instance;
    }

    private LyricsLoader(Context con){
        final okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request newRequest;

                        newRequest = request.newBuilder()
                                .addHeader("Cache-Control", String.format("max-age=%d,max-stale=%d", Integer.valueOf(60 * 60 * 24 * 7), Integer.valueOf(31536000)))
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .client(okHttpClient)
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                     //TODO: fix to match below
                        return super.stringConverter(type, annotations, retrofit);
                    }
                })
                .build();


//        RestAdapter.Builder builder = new RestAdapter.Builder()
//                .setEndpoint(BASE_API_URL)
//                .setRequestInterceptor(interceptor)
//                .setConverter(new Converter() {
//                    @Override
//                    public Object fromBody(TypedInput arg0, Type arg1)
//                            throws ConversionException {
//
//                        try {
//                            BufferedReader br = null;
//                            StringBuilder sb = new StringBuilder();
//
//                            String line;
//
//                            br = new BufferedReader(new InputStreamReader(arg0.in()));
//                            while ((line = br.readLine()) != null) {
//                                sb.append(line);
//                                sb.append('\n');
//                            }
//                            return sb.toString();
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }
//
//                    @Override
//                    public TypedOutput toBody(Object arg0) {
//                        return null;
//                    }
//                })
//                .setClient(new OkClient(okHttpClient));

         service = retrofit
                .create(LyricsRestService.class);
    }

    public void getLyrics(String artist, String title, Callback<String> callback){
        service.getLyrics(artist,title,callback);
    }

    private interface LyricsRestService {
        @Headers("Cache-Control: public")
        @GET("/lyrics")
        void getLyrics(@Query("artist") String artist, @Query("title") String title, Callback<String> callback);
    }

}
