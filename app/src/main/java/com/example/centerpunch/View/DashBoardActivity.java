package com.example.centerpunch.View;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.R;
import com.example.centerpunch.databinding.ActivityDashBoardBinding;
import com.example.centerpunch.databinding.ActivityMainBinding;

public class DashBoardActivity extends BaseActivity implements NetWorkCheck.NetworkChangeListener {
    private AlertDialog noInternetDialog;
    NetWorkCheck netWorkCheck;
    private AlertDialog loaderDialog;
    ActivityDashBoardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        binding = ActivityDashBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!NetWorkCheck.isInternetAvailable(this)) {
            showNoInternetDialog();
        }

        binding.btnCenter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent inn = new Intent(DashBoardActivity.this, MainActivity.class);
                startActivity(inn);
            }
        });

        binding.btnBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_snackbar, null);
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText("This Feature Not Available now  !");
                ImageView icon = layout.findViewById(R.id.toast_icon);
                icon.setImageResource(R.drawable.error); // Replace with your drawable

                Toast toast = new Toast(getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                toast.show();
                // Toast.makeText(DashBoardActivity.this,"This feature not available now",Toast.LENGTH_SHORT).show();
            }
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
            finish();
        });

        noInternetDialog.show();
    }


}