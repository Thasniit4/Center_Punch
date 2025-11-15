package com.example.centerpunch.BaseMethod;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centerpunch.R;

public class BaseActivity extends AppCompatActivity {
    ProgressDialog progressBar ;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public String EmpCode,branchid;
    public String EmpName,ToDate;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    public String ImageURL;

    private AlertDialog loaderDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // sessionManager = new SessionManager(this);
        sharedPreferences=getSharedPreferences("PUNCH", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        EmpCode = sharedPreferences.getString("EmpCode",null);
        EmpName = sharedPreferences.getString("EmpName",null);
        ToDate = sharedPreferences.getString("ToDate",null);
        branchid = sharedPreferences.getString("branchid",null);
        ImageURL = sharedPreferences.getString("IMAGE_URL", null);
        Log.d("SavedImageUrl", "URL = " + ImageURL);
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);//you can cancel it by pressing back button
        progressBar.setMessage("Loading...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
//    public void HideProgress() {
//        if(progressBar.isShowing()){
//            progressBar.dismiss();
//        }
//    }
//
//    public void ShowProgress() {
//        if(!progressBar.isShowing()){
//            progressBar.show();//displays the progress bar
//        }
//    }
    // Start 1-minute timeout
    public void startTimeout(long timeoutMillis) {
        cancelTimeout();
        timeoutHandler = new Handler();
        timeoutRunnable = () -> {
            hideLoader();
          //  customToast();
            Toast.makeText(this, "Server is taking too long to respond. Please try again later.", Toast.LENGTH_LONG).show();
        };
        timeoutHandler.postDelayed(timeoutRunnable, timeoutMillis);
    }

    // Cancel timeout
    public void cancelTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    public void customToast(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_snackbar, null);

// Customize the text and icon if needed
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText("Server is taking too long to respond. Please try again later.!");

        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.tick); // Replace with your drawable

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
        toast.show();

    }

    protected void showLoader(String message) {
        if (loaderDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.custom_loader, null);
            TextView loaderText = view.findViewById(R.id.loaderText);
            loaderText.setText(message);
            builder.setView(view);
            builder.setCancelable(false);
            loaderDialog = builder.create();
        }
        loaderDialog.show();
    }

    protected void hideLoader() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            loaderDialog.dismiss();
        }
    }

}

