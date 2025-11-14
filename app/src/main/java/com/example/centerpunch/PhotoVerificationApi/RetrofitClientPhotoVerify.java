package com.example.centerpunch.PhotoVerificationApi;


import com.example.centerpunch.Network.ApiInterface;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.centerpunch.BuildConfig;



public class RetrofitClientPhotoVerify {

    private static Retrofit retrofit;
    private static String BASE_URL ="https://api-preproduction.signzy.app/api/v3/";
    private static RetrofitClientPhotoVerify instance = null;
    ApiInterface apiInterface;



    private RetrofitClientPhotoVerify() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization",  BuildConfig.API_TOKEN) // <-- add your token here
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(client.newBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static synchronized RetrofitClientPhotoVerify getInstance() {
        if (instance == null) {
            instance =  new RetrofitClientPhotoVerify();
        }
        return instance;
    }
    public ApiInterface getMyApi() {
        return apiInterface;
    }

}