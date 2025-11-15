package com.example.centerpunch.Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;


public class NetWorkCheck {

    private static final String TAG = "NetWorkCheck";

    // Listener interface
    public interface NetworkChangeListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }

    private static NetworkChangeListener listener;

    // Track internet availability
    private static boolean isInternetAvailable = false;

    // Store callback + manager so we can unregister safely
    private static ConnectivityManager.NetworkCallback networkCallback;
    private static ConnectivityManager connectivityManager;

    // Set listener
    public static void setNetworkChangeListener(NetworkChangeListener newListener) {
        listener = newListener;
    }

    // Register callback
    public static void registerNetworkCallback(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        // Avoid double registration
        if (networkCallback != null) {
            Log.e(TAG, "Callback already registered");
            return;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {

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
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                isInternetAvailable = hasInternet;

                if (listener != null && hasInternet) {
                    listener.onNetworkAvailable();
                }
            }
        };

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
            Log.e(TAG, "Network callback registered");
        } catch (Exception e) {
            Log.e(TAG, "Error registering callback", e);
        }
    }

    // Unregister callback safely
    public static void unregisterNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.e(TAG, "Network callback unregistered");
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network callback", e);
            }
        }
        networkCallback = null;
    }

    public static boolean isInternetAvailable() {
        return isInternetAvailable;
    }
}






