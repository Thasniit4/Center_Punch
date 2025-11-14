package com.example.centerpunch.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

public class NetWorkCheck {

    private static final String TAG = "NetWorkCheck";
    private static boolean isInternetAvailable = false;
    private static NetworkChangeListener listener;

    public interface NetworkChangeListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    public static void setNetworkChangeListener(NetworkChangeListener newListener) {
        listener = newListener;
    }

    public static void registerNetworkCallback(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) return;

        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.e(TAG, "Network available");
                isInternetAvailable = true;
                if (listener != null) listener.onNetworkAvailable();
            }

            @Override
            public void onLost(Network network) {
                Log.e(TAG, "Network lost");
                isInternetAvailable = false;
                if (listener != null) listener.onNetworkLost();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
                boolean hasInternet = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                isInternetAvailable = hasInternet;
                if (listener != null && hasInternet) listener.onNetworkAvailable();
            }
        });
    }

    public static boolean isInternetAvailable(Context context) {
        if (isInternetAvailable) return true;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}





