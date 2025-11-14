package com.example.centerpunch.LoginApi;

import com.example.centerpunch.Network.ApiInterface;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientLogin {
    private static Retrofit retrofit;
    private static String BASE_URL ="https://lms.asirvad.com/mfpublichrmsapi/";
    private static RetrofitClientLogin instance = null;
    ApiInterface apiInterface;
    private RetrofitClientLogin() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(7, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(7, TimeUnit.MINUTES) // write timeout
                .readTimeout(7, TimeUnit.MINUTES) // read timeout
                .build();
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(client.newBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }
    public static synchronized RetrofitClientLogin getInstance() {
        if (instance == null) {
            instance = new RetrofitClientLogin();
        }
        return instance;
    }
    public ApiInterface getMyApi() {
        return apiInterface;
    }
}

