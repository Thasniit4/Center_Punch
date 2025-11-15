package com.example.centerpunch.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;

import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.BaseMethod.GpsTracker;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PunchApi.PunchRequest;
import com.example.centerpunch.PunchApi.PunchResponse;
import com.example.centerpunch.PunchApi.RetrofitClient;
import com.example.centerpunch.R;
import com.example.centerpunch.databinding.ActivityLocationChangeRequestBinding;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Location_Change_Request extends BaseActivity implements NetWorkCheck.NetworkChangeListener{

    private AlertDialog noInternetDialog;
    String selectedItem,BranchId,CurrentLat,CurrentLong,city,value,CenterId,ActivityName,Alert;
    int Req;
    float lat2,long2,GeoLat,GeoLong;
    private static final int REQUEST_LOCATION = 1;
    ActivityLocationChangeRequestBinding binding;
    PunchResponse punchResponse;
    PunchRequest punchRequest;
    private Handler sessionHandler;
    private Runnable sessionRunnable;
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_change_request);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        if (!NetWorkCheck.isInternetAvailable()) {
            showNoInternetDialog();
        }
        binding = ActivityLocationChangeRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionHandler = new Handler();
        sessionRunnable = () -> {
            Toast.makeText(Location_Change_Request.this, "Session expired due to inactivity", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Location_Change_Request.this, LoginPage.class);
            startActivity(intent);
            finish();
        };
        resetSessionTimer();
        selectedItem = getIntent().getStringExtra("selectedItem");
        BranchId=getIntent().getStringExtra("BranchId");
        GeoLat = getIntent().getFloatExtra("GeoLat", 0.0f);
        GeoLong = getIntent().getFloatExtra("GeoLong", 0.0f);
        CenterId=getIntent().getStringExtra("CenterId");
        ActivityName = getIntent().getStringExtra("ActivityName");

        if (ActivityName.equals("MainActivity")) {
//        if (selectedItem != null && BranchId != null ) {
            binding.EmpCode.setText(EmpCode);
            binding.EmpName.setText(EmpName);
            binding.BranchId.setText(BranchId);
            binding.CenterName.setText(selectedItem);
            binding.OldLat.setText(String.valueOf(GeoLat));
            binding.OldLong.setText(String.valueOf(GeoLong));
        }
        getLocation();
        binding.request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value=EmpCode+"¥"+EmpName+"¥"+BranchId+"¥"+CenterId +"¥"+binding.CenterName.getText()+
                        "¥"+lat2+"¥"+long2+"¥"+binding.OldLat.getText()+"¥"+binding.OldLong.getText()+"¥"+city;
                showChangeCenter(value);
                //  FDA_Request(value);
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                Location_Change_Request.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                Location_Change_Request.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            GpsTracker gps = new GpsTracker(getApplicationContext());
            CurrentLat = String.valueOf(gps.getLatitude());
            CurrentLong = String.valueOf(gps.getLongitude());
            lat2 = Float.parseFloat(CurrentLat);
            long2 = Float.parseFloat(CurrentLong);
            getAddressFromLatLng(lat2,long2);
            binding.CurLat.setText(CurrentLat);
            binding.CurLong.setText(CurrentLong);
            if (lat2 == 0.0 || long2 == 0.0) {
                Toast.makeText(this, "Turn on Gps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAddressFromLatLng(float latitude, float longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                city = address.getLocality(); // City name
                binding.CurPlace.setText(city);
                //      Toast.makeText(this, "" + city, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get address for this location.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void FDA_Request(String value) {
        showLoader("");
        punchRequest = new PunchRequest();
        punchRequest.setpFlag("LocationModification");
        punchRequest.setEmpCode(value);
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                punchResponse = response.body();
                hideLoader();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    Req= punchResponse.getCenterpunchdata().get(0).getReqcount();
                    Alert= punchResponse.getCenterpunchdata().get(0).getAlert();

                    if(Req> 0){
                        showSuccessAlert();
                    } else if(Alert != null && Alert.equals("1")) {
                        showMvtAlert();
                    }
                    else {
                        Toast.makeText(Location_Change_Request.this, "No alert triggered", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Location_Change_Request.this, "Network request failed", Toast.LENGTH_SHORT).show();
                    hideLoader();
                }
            }

            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(Location_Change_Request.this, "Network request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showSuccessAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sDialog.setTitleText("Request");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setCustomImage(R.drawable.success);
        sDialog.setContentText("Request successful. Recommendation has been sent to BH.");
        sDialog.setCancelable(false);
        sDialog.show();
        sDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onBackPressed();
            }
        });
    }

    private void showMvtAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("You already updated");
        sDialog.setCancelable(false);
        sDialog.show();
        sDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onBackPressed();
            }
        });
    }
    private void showChangeCenter(String value) {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sDialog.setTitleText("Confirmation");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("Do you want to change the center?");
        sDialog.setCancelable(false);
        sDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                FDA_Request(value);
            }
        });
        sDialog.setCancelButtonBackgroundColor(R.color.LightBlue);
        sDialog.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation(); // Just dismiss
            }
        });
        sDialog.show();
    }
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetSessionTimer(); // Reset timer on any user interaction
    }

    private void resetSessionTimer() {
        sessionHandler.removeCallbacks(sessionRunnable);
        sessionHandler.postDelayed(sessionRunnable, SESSION_TIMEOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionHandler.removeCallbacks(sessionRunnable);
        NetWorkCheck.setNetworkChangeListener(null);
        NetWorkCheck.unregisterNetworkCallback();
    }

    @Override
    public void onNetworkAvailable() {
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
}