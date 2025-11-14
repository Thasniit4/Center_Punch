package com.example.centerpunch.View;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import retrofit2.Call;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.centerpunch.Network.ApiInterface;
import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.BaseMethod.utility;
import com.example.centerpunch.LoginApi.LoginRequest;
import com.example.centerpunch.LoginApi.LoginResponse;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PunchApi.PunchRequest;
import com.example.centerpunch.PunchApi.PunchResponse;
import com.example.centerpunch.PunchApi.RetrofitClient;
import com.example.centerpunch.R;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Callback;
import retrofit2.Response;

public class LoginPage extends BaseActivity implements NetWorkCheck.NetworkChangeListener {

    ApiInterface apiInterface;
    EditText user, pass;
    Button submit;
    int empcode;
    String key = "7x!A%D*G-KaPdSgV";
    String iv = "7x!A%D*G-KaPdSgV";
    String empname, todate, branchid;
    boolean isAllFieldsChecked = false;
    PunchResponse punchResponse;

    private AlertDialog loaderDialog;

    NetWorkCheck netWorkCheck;
    private AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        user = findViewById(R.id.user);
        pass = findViewById(R.id.pass);
        submit = findViewById(R.id.submit);
        submit.setBackgroundResource(R.drawable.cus_btm);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("capturedImage");
        editor.putBoolean("verifiedThisSession", false);
        editor.apply();

        if (!NetWorkCheck.isInternetAvailable(this)) {
            showNoInternetDialog();
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAllFieldsChecked = CheckAllFields(); // Call once
                Log.d("LoginCheck", "isAllFieldsChecked = " + isAllFieldsChecked);
                Log.d("LoginCheck", "Username: " + user.getText().toString());
                Log.d("LoginCheck", "Password: " + pass.getText().toString());
                if (isAllFieldsChecked ) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_snackbar, null);
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Logged Successfully!");
                    ImageView icon = layout.findViewById(R.id.toast_icon);
                    icon.setImageResource(R.drawable.tick); // Replace with your drawable
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                    toast.show();

                    // All good — proceed with login
                    getUser(user.getText().toString());
                } else {

                    // Validation failed — focus on the first invalid field
                    if (user.getError() != null) {
                        user.requestFocus();
                    } else if (pass.getError() != null) {
                        pass.requestFocus();
                    }

                    else{
                        showNoInternetDialog();
                    }

                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_snackbar, null);
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Login failed!");
                    ImageView icon = layout.findViewById(R.id.toast_icon);
                    icon.setImageResource(R.drawable.tick); // Replace with your drawable
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                    toast.show();

                }
            }
        });


    }

    private void getLogin(String userName, String password) {
        showLoader("");
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId(Integer.valueOf(userName));
        try {
            password = utility.Encrypt(password, key);
            Log.e("TAG", "getLogin: " + password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loginRequest.setPassword(password);
        loginRequest.setTypeId(1);
        loginRequest.setDeviceId("");
        loginRequest.setModuleId(2);
        Call<LoginResponse> call = RetrofitClient.getInstance().getMyApi().login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                hideLoader();
                if (user.getText().toString().length() == 6) {

                    if (response.isSuccessful()) {
                        LoginResponse baseresponse = response.body();
                        hideLoader();
                        if (baseresponse.getStatus().equals("SUCCESS")) {
                            editor.putInt("userid", baseresponse.getData().getUserId());
                            editor.apply();
                            showLoader("");
                            getUser(user.getText().toString());
                        } else if (baseresponse.getStatus().equals("ERROR")) {
                            Toast.makeText(LoginPage.this, "Login failed", LENGTH_SHORT).show();
                        } else if (baseresponse.getStatus().equals("FAIL")) {
                            String respns = baseresponse.getResponseMsg();
                            pass.setError(respns);
                            isAllFieldsChecked = CheckAllFields();
                        } else {
                            Toast.makeText(LoginPage.this, "Login failed", LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginPage.this, "You are not authorised to login", LENGTH_SHORT).show();
                    }
                } else {
                    hideLoader();
                    Toast.makeText(LoginPage.this, "An error has occurred", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(LoginPage.this, "Network request failed", Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean CheckAllFields() {
        String username = user.getText().toString().trim();
        String password = pass.getText().toString().trim();

        if (username.isEmpty() && password.isEmpty()) {
            user.setError("Enter username and password");
            //  pass.setError("Enter username and password");
            Log.d("Validation", "Both fields empty");
            return false;
        }

        if (username.isEmpty()) {
            user.setError("Please enter username");
            Log.d("Validation", "Username empty");
            return false;
        }

        if (username.length() != 6) {
            user.setError("Username must be 6 characters");
            Log.d("Validation", "Username too short");
            return false;
        }

        if (password.isEmpty()) {
            pass.setError("Please enter password");
            Log.d("Validation", "Password empty");
            return false;
        }

        return true;
    }


    private void getUser(String userid) {

       showLoader("Logging In Please Wait ...");
        startTimeout(60000); // 1-minute timeout
        String.valueOf(userid);
        int version = 4;
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setEmpCode(userid + "¥" + version);
        punchRequest.setpFlag("Login");
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                cancelTimeout();  // Cancel timeout
                hideLoader();
                punchResponse = response.body();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    String Alert = punchResponse.getCenterpunchdata().get(0).getAlert();
                    String PostId = String.valueOf(punchResponse.getCenterpunchdata().get(0).getPosTID());
                    empcode = punchResponse.getCenterpunchdata().get(0).getEmPCODE();
                    editor.putString("EmpCode", String.valueOf(empcode));
                    editor.apply();
                    empname = String.valueOf(punchResponse.getCenterpunchdata().get(0).getEmPNAME());
                    editor.putString("EmpName", empname);
                    editor.apply();
                    branchid = String.valueOf(punchResponse.getCenterpunchdata().get(0).getBrancHID());
                    editor.putString("branchid", branchid);
                    editor.apply();
                    if (Alert != null && Alert.equals("NOT")) {
                        Toast.makeText(LoginPage.this, "A new version of the application is available. Please update to the latest version for a better experience.", Toast.LENGTH_LONG).show();
                    } else if (PostId.equals("16") || PostId.equals("32")) {
                        GetDbData(userid);

                    } else if (PostId.equals("179") || PostId.equals("103") || PostId.equals("12") || PostId.equals("139")) {
                        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        GetDeviceId(userid, deviceId);
                    } else {
                        assert Alert != null;
                        if (Alert.equals("NOTPUNCH")) {
                            Toast.makeText(LoginPage.this, "Login is allowed only with your employee code. You can't log in on another device once logged in.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginPage.this, "You are not authorised to login....", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    hideLoader();
                    Toast.makeText(LoginPage.this, "You are not authorised to login.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                cancelTimeout();   // Cancel timeout on failure
                hideLoader();
                Toast.makeText(LoginPage.this, "Please turn on your network connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void GetDeviceId(String userid, String dev) {
        showLoader("");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setEmpCode(userid + "¥" + dev);
        punchRequest.setpFlag("TOKEN_CHECK");
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                hideLoader();
                punchResponse = response.body();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    String Alert = punchResponse.getCenterpunchdata().get(0).getAlert();
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    if (Alert.equals("insert") || Alert.equals("success")) {
                        Intent inn = new Intent(LoginPage.this, RITC_RTSE_PUNCH.class);
                        startActivity(inn);
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_snackbar, null);
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Logged Successfully!");
                        ImageView icon = layout.findViewById(R.id.toast_icon);
                        icon.setImageResource(R.drawable.tick); // Replace with your drawable
                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                        toast.show();
                      //  Toast.makeText(LoginPage.this, "Login successfully", LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginPage.this, "Login is allowed only with your employee code. You can't log in on another device once logged in.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    hideLoader();
                    Toast.makeText(LoginPage.this, "You are not authorised to login..", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(LoginPage.this, "Network request failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void GetDbData(String EmpCode) {
      showLoader("Logging In");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setEmpCode(EmpCode);
        punchRequest.setpFlag("EmpPhotoCheck");
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                hideLoader();
                punchResponse = response.body();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    float ReqCount = punchResponse.getCenterpunchdata().get(0).getReqcount();
                    empcode = punchResponse.getCenterpunchdata().get(0).getEmPCODE();
                    Log.d("EMpCode", "EMp code" + empcode);
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                    if ((ReqCount == 0.0f)) {
                        Intent inn = new Intent(LoginPage.this, DbImageComparison.class);
                        startActivity(inn);

                        // Toast.makeText(LoginPage.this, "Db Image", Toast.LENGTH_SHORT).show();
                        Log.d("DB_CHECK", "Alert: " + ReqCount + ", EmpCode: " + empcode);
                    }


                    else {
                        Intent in = new Intent(LoginPage.this, DashBoardActivity.class);
                        startActivity(in);
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_snackbar, null);
                        TextView text = layout.findViewById(R.id.toast_text);
                        text.setText("Logged Successfully!");

                        ImageView icon = layout.findViewById(R.id.toast_icon);
                        icon.setImageResource(R.drawable.tick); // Replace with your drawable

                        Toast toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                        toast.show();
                      //  Toast.makeText(LoginPage.this, "Login successfully", LENGTH_SHORT).show();
                    }
                }

                else {
                    hideLoader();
                    Toast.makeText(LoginPage.this, "You are not authorised to login..", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(LoginPage.this, "Network request failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onBackPressed() {
        finishAffinity();
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
    protected void onDestroy() {
        super.onDestroy();
        // Always clear the listener when the activity is destroyed
        NetWorkCheck.setNetworkChangeListener(null);
    }


}





