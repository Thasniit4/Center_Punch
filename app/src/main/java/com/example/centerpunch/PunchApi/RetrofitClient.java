package com.example.centerpunch.PunchApi;


import com.example.centerpunch.Network.ApiInterface;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static String BASE_URL ="https://amfluat.asirvad.com/AlertEscaltionPublic/";
    //private static String BASE_URL ="https://apps.asirvad.com/AlertEscalationpublicAPI/";   // live
   //private static String BASE_URL ="https://lms.asirvad.com/AlertEscalationpublicAPI/";   //lms
   private static RetrofitClient instance = null;
    ApiInterface apiInterface;

    private RetrofitClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(2, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
                .readTimeout(2, TimeUnit.MINUTES) // read timeout
                .build();

        retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .client(client.newBuilder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    public ApiInterface getMyApi() {
        return apiInterface;
    }

}
