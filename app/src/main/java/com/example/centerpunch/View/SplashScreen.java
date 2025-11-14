package com.example.centerpunch.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity implements NetWorkCheck.NetworkChangeListener {


    private AlertDialog noInternetDialog;
    NetWorkCheck netWorkCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);

        // Initial check
        if (NetWorkCheck.isInternetAvailable(this)) {
            goToLogin();
        } else {
            showNoInternetDialog();
        }
    }

    @Override
    public void onNetworkAvailable() {
        Log.e("SplashScreen", "Internet is back!");
        runOnUiThread(() -> {
            if (noInternetDialog != null && noInternetDialog.isShowing()) {
                noInternetDialog.dismiss();
            }
            goToLogin();
        });
    }

    @Override
    public void onNetworkLost() {
        Log.e("SplashScreen", "Internet lost!");
        runOnUiThread(this::showNoInternetDialog);
    }


    private void goToLogin() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, LoginPage.class));
            finish();
        }, 2000);
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) return;

        View view = getLayoutInflater().inflate(R.layout.dialog_no_internet, null);

        noInternetDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        Button btnExit = view.findViewById(R.id.btn_exit);

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        });

        btnExit.setOnClickListener(v -> {
            noInternetDialog.dismiss();
            finish();
        });

        noInternetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetWorkCheck.setNetworkChangeListener(null);
    }

}
