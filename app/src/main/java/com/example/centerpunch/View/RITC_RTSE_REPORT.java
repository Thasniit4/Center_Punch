package com.example.centerpunch.View;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.R;

public class RITC_RTSE_REPORT extends AppCompatActivity implements NetWorkCheck.NetworkChangeListener {
    private AlertDialog noInternetDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ritc_rtse_report);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        if (!NetWorkCheck.isInternetAvailable()) {
            showNoInternetDialog();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onNetworkAvailable() {
        Log.e("LoginPage", "Internet is back!");
        runOnUiThread(() -> {
            if (noInternetDialog != null && noInternetDialog.isShowing()) {
                noInternetDialog.dismiss();
            }
        });
    }

    @Override
    public void onNetworkLost() {
        Log.e("LoginPage", "Internet lost!");
        runOnUiThread(this::showNoInternetDialog);
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
            finishAffinity();
        });

        noInternetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetWorkCheck.setNetworkChangeListener(null);
        NetWorkCheck.unregisterNetworkCallback();
    }
}