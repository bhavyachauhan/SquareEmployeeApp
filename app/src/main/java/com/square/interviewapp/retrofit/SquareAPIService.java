package com.square.interviewapp.retrofit;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.square.interviewapp.utils.gson.DataDeserializer;
import com.square.interviewapp.model.EmployeeDetails;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SquareAPIService {

    public static final String INTERNET_DISCONNECTED_BROADCAST_KEY = "internet_disconnected_broadcast_key";
    public static final int NETWORK_TIMEOUT_SECONDS = 10;

    final private SquareEmployeeService service;
    final private Retrofit retrofit;
    private static SquareAPIService apiService;
    private final WeakReference<Context> contextWeakReference;

    public static SquareAPIService getRetrofitService(final Context context) {
        if (apiService == null) {
            apiService = new SquareAPIService(context);
        }
        return apiService;
    }

    public static SquareEmployeeService getSquareAPIService(final Context context) {
        return getRetrofitService(context).service;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private SquareAPIService(final Context context) {
        this.contextWeakReference = new WeakReference<>(context);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(EmployeeDetails.class, new DataDeserializer<EmployeeDetails>())
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl("https://s3.amazonaws.com/sq-mobile-interview/")
                .client(getOkHttpClientWithNetworkConnectionInterceptor(NETWORK_TIMEOUT_SECONDS))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(SquareEmployeeService.class);
    }

    @SuppressWarnings("SameParameterValue")
    public OkHttpClient getOkHttpClientWithNetworkConnectionInterceptor(final int timeout) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);

        builder.addInterceptor(new ClientRequestInterceptor() {
            @Override
            public boolean isInternetConnected() {
                return contextWeakReference.get() != null && isInternetAvailable(contextWeakReference.get());
            }

            @Override
            public void onInternetDisconnected() {
                if (contextWeakReference.get() != null) {
                    LocalBroadcastManager.getInstance(contextWeakReference.get()).sendBroadcast(new Intent(INTERNET_DISCONNECTED_BROADCAST_KEY));
                }
            }
        });

        return builder.build();
    }

    private boolean isInternetAvailable(final Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static abstract class ClientRequestInterceptor implements Interceptor {

        public abstract boolean isInternetConnected();

        public abstract void onInternetDisconnected();

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            if (!isInternetConnected()) {
                onInternetDisconnected();
            }

            return chain.proceed(request);
        }

    }

}
