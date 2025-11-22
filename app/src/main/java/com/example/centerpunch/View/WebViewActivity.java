package com.example.centerpunch.View;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PhotoVerificationApi.RetrofitClientPhotoVerify;
import com.example.centerpunch.PhotoVerificationCompleteApi.VerificationCompleteRequest;
import com.example.centerpunch.PhotoVerificationCompleteApi.VerificationCompleteResponse;
import com.example.centerpunch.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewActivity extends AppCompatActivity implements NetWorkCheck.NetworkChangeListener {
    private String base64Image = null;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ValueCallback<Uri[]> filePathCallback;

    private Uri cameraImageUri;
    private static final int FILE_CHOOSER_REQUEST = 101;
    private WebView webView;
    private String token;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean verificationTriggered = false;
    private AlertDialog noInternetDialog;
    private NetWorkCheck netWorkCheck;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        WebView.setWebContentsDebuggingEnabled(true);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        if (!NetWorkCheck.isInternetAvailable()) {
            showNoInternetDialog();
        }
    //    String backCamera = getIntent().getStringExtra("flipCamera");
        String videoUrl = getIntent().getStringExtra("videoUrl");
        token = getIntent().getStringExtra("token");
        String faceMatch = getIntent().getStringExtra("faceMatch");
        Log.d("Video_token", "getToken " + token);
        requestCameraPermission();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                });
            }
        });
        if (videoUrl != null) {
            webView.loadUrl(videoUrl);

            Log.d("VideoURL", "Loaded: " + videoUrl);
            startCheckingForCompletion();

        } else {

            Toast.makeText(this, "No video URL provided", LENGTH_SHORT).show();
            showNoInternetDialog();
        }

    }



    private void startCheckingForCompletion() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (verificationTriggered) return; // stop once triggered
          //      Log.d("verificationTriggered","verification first " +verificationTriggered);
                webView.evaluateJavascript(
                        "(function() { return document.body.innerText; })();",
                        html -> {
                            Log.d("WebViewText", "Checking page text...");
                            requestCameraPermission();
                            if (html.contains("Verification Completed") || html.contains("Verification Failed") || html.contains("Verification Successful")) {

                                Log.d("WebViewText", "✅ Detected verification completion!");
                                verificationTriggered = true;
          //                      Log.d("verificationTriggered","verification second " +verificationTriggered);
                                verifyPhotoComplete(token);

                            } else {
                                // keep checking every 2 seconds
                                handler.postDelayed(this, 2000);
                            }
                        }
                );
            }
        }, 2000);
    }




    // ✅ Ask for camera permission
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    // ✅ Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Camera permission denied", LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_PERMISSION_CODE && resultCode == RESULT_OK) {
            if (filePathCallback != null && data != null) {
                Uri result = data.getData();
                filePathCallback.onReceiveValue(new Uri[]{result});
                filePathCallback = null;
            }
        }
    }


    // ✅ Call your second API automatically after failure detected
    private void verifyPhotoComplete(String token) {
        VerificationCompleteRequest request = new VerificationCompleteRequest();
        request.setToken(token);
        Log.d("token", "setToken: " + token);

        Call<VerificationCompleteResponse> call = RetrofitClientPhotoVerify
                .getInstance()
                .getMyApi()
                .verifyPhotoCompletion(request);

        call.enqueue(new Callback<VerificationCompleteResponse>() {
            @Override
            public void onResponse(Call<VerificationCompleteResponse> call, Response<VerificationCompleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VerificationCompleteResponse verificationCompleteResponse = response.body();
                    String error = verificationCompleteResponse.getResult().getFaceMatch().getMessage();
                    String matchPercentageStr = verificationCompleteResponse.getResult().getFaceMatch().getMatchPercentage();  // e.g. "99.00%"
                    boolean allowCamera = Boolean.parseBoolean(verificationCompleteResponse.getEssentials().getAllowCameraSwitch());
                    boolean liveness = verificationCompleteResponse.getResult().getPassiveLiveliness().getLiveness();

                    Log.d("matchPercentage", "matchPercentage1: " + matchPercentageStr);


                    if (matchPercentageStr != null && matchPercentageStr.endsWith("%")) {
                        matchPercentageStr = matchPercentageStr.replace("%", ""); // remove %
                        Log.d("matchPercentage", "matchPercentage2: " + matchPercentageStr);
                    }

                    try {
                        double matchValue = Double.parseDouble(matchPercentageStr);

                        if (error != null && error.equals("Verification completed with negative result") && liveness == false) {
                            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putBoolean("verifiedThisSession", false)
                                    .putBoolean("Verification Failed", true)
                                    .remove("capturedImage")
                                    .apply();
                            Toast.makeText(WebViewActivity.this, "Verification failed", Toast.LENGTH_LONG).show();
                            showBeautifulAlertDialog();
                            return;
                        } else {
                            assert error != null;
                            if (error.equals("Verification completed with positive result") && matchValue > 75.00 && liveness == true) {
                                String capturedImage = verificationCompleteResponse.getResult().getCapturedImage();
                                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("capturedImage", capturedImage);
                                editor.putBoolean("verifiedThisSession", true);
                                editor.putBoolean("Verification Failed", false);
                                editor.apply();
                                getOnBackPressedDispatcher().onBackPressed();
                                Log.d("savedImage", "Saved Image4" + capturedImage);
                                //  startActivity(new Intent(WebViewActivity.this, MainActivity.class));
                                Log.d("matchPercentage", "matchPercentage3: " + matchPercentageStr);

                            } else {
                                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putBoolean("verifiedThisSession", false)
                                        .putBoolean("verificationFailed", true)
                                        .remove("capturedImage")
                                        .apply();
                                showBeautifulAlertDialog();
                                Log.d("matchPercentage", "matchPercentage4: " + matchPercentageStr);

                            }
                        }


                    } catch (NumberFormatException e) {
                        Toast.makeText(WebViewActivity.this, "Invalid match percentage value", Toast.LENGTH_SHORT).show();
                        Log.e("matchPercentage", "Error parsing matchPercentageStr: " + matchPercentageStr, e);
                    }

                } else {
                    Toast.makeText(WebViewActivity.this, "Verification Error! Try again.", LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        getOnBackPressedDispatcher().onBackPressed();
                    }, 2000);
                }
            }

            @Override
            public void onFailure(Call<VerificationCompleteResponse> call, Throwable t) {
                Log.e("Verification Failed on failure", t.getMessage(), t);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(WebViewActivity.this, MainActivity.class));
                    finish();
                }, 2000);
            }
        });
    }

    private void showBeautifulAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.custom_alert, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // Optional: make dialog background transparent (for rounded corners)
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Button click
        Button okButton = view.findViewById(R.id.btnOk);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            getOnBackPressedDispatcher().onBackPressed();
            // your logic
        });

        dialog.show();
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
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null); // Stop checking loop
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (webView != null) {
                webView.onResume();
                webView.reload();
                startCheckingForCompletion();
                // Force reload the page when returning
           }
    }
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
        NetWorkCheck.setNetworkChangeListener(null);
        NetWorkCheck.unregisterNetworkCallback();
    }
}

