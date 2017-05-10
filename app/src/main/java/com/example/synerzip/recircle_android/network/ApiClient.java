package com.example.synerzip.recircle_android.network;

import com.example.synerzip.recircle_android.utilities.RCWebConstants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Snehal Tembare on 9/3/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */

public class ApiClient{
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        if (null == retrofit) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RCWebConstants.RC_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
