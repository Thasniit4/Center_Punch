package com.example.centerpunch.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.BaseMethod.GpsTracker;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PhotoVerificationApi.PhotoVerificationRequest;
import com.example.centerpunch.PhotoVerificationApi.PhotoVerificationResponse;
import com.example.centerpunch.PhotoVerificationApi.RetrofitClientPhotoVerify;
import com.example.centerpunch.PunchApi.PunchRequest;
import com.example.centerpunch.PunchApi.PunchResponse;
import com.example.centerpunch.PunchApi.RetrofitClient;
import com.example.centerpunch.R;
import com.example.centerpunch.UploadPhoto.UploadRequest;
import com.example.centerpunch.UploadPhoto.UploadResponse;
import com.example.centerpunch.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements NetWorkCheck.NetworkChangeListener {

    private NetWorkCheck netWorkCheck;

    private AlertDialog noInternetDialog;

    private androidx.appcompat.app.AlertDialog loaderDialog;

    private WebView myWebView;
    private static final int REQUEST_LOCATION = 1;
    private static final int CAME_REQ = 100;
    private String base64Image = null;
    File file;

    PhotoVerificationResponse photoVerificationResponse;
    String filePath,fileName;
    ActivityMainBinding binding;
    LocationManager locationManager;
    private String selectedItem;
    PunchResponse punchResponse;
    List<String> CenterNames = new ArrayList<String>();
    HashMap<String, String> centerMap = new HashMap<>();
    Float GeoLat, GeoLong,lat2,long2;
    String Center, city,val,time,val1,CurrentLat,CurrentLong,BranchId,CenterId,Alert,CentId,selectedCenterId;
    private ArrayList<String> ListNames = new ArrayList<String>();
    private Handler sessionHandler;
    private Runnable sessionRunnable;
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000;
    UploadResponse uploadResponse;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        if (!NetWorkCheck.isInternetAvailable(this)) {
            showNoInternetDialog();
        }


        //   loadSavedImages();
//        SessionManager sessionManager = new SessionManager(this);
//        sessionManager.getUserId();
        sessionHandler = new Handler();
        sessionRunnable = () -> {
            Toast.makeText(MainActivity.this, "Session expired due to inactivity", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, LoginPage.class);
            startActivity(intent);
            finish();
        };
        resetSessionTimer();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day == Calendar.SUNDAY) {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("The app is not available on Sundays.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish(); // Close the app
                        }
                    })
                    .show();
        }
        binding.linear2.setVisibility(View.GONE);
        binding.linear3.setVisibility(View.GONE);
        getFDAList();
        getCenterList();
        binding.CenterName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Select Center Name")) {
                    binding.imageView.setImageDrawable(null);
                    base64Image = "";
                    CenterId="";
                    selectedCenterId="";
                    binding.submit.setVisibility(View.GONE);
                    binding.linear2.setVisibility(View.GONE);

                    //                } else if (selectedItem.isEmpty() || selectedItem.equals("")) {
//                    NoDataAlert();
                } else {
                    binding.imageView.setImageDrawable(null);
                    binding.submit.setVisibility(View.VISIBLE);
                    selectedCenterId = centerMap.get(selectedItem);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_snackbar, null);
                    TextView text = layout.findViewById(R.id.toast_text);
                    text.setText("Center ID!" + selectedCenterId);
                    ImageView icon = layout.findViewById(R.id.toast_icon);
                    icon.setImageResource(R.drawable.tick); // Replace with your drawable
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
                    toast.show();
                  //  Toast.makeText(MainActivity.this, "Center ID: " + selectedCenterId , Toast.LENGTH_LONG).show();
                    GetCenterLocation(selectedItem,selectedCenterId);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        adapter.notifyItemInserted(0);
//                        check();
//                        binding.swipeRefreshLayout.setRefreshing(false);
//                        Toast.makeText(MainActivity.this, "Refreshed!", Toast.LENGTH_SHORT).show();
//                    }
//                }, 2000);
//            }
//        });

        binding.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent CameIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (CameIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(CameIntent, CAME_REQ);

                }


            }
        });
        binding.photoVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetWorkCheck.isInternetAvailable(MainActivity.this)) {
                    // ✅ Internet is available → proceed with photo verification
                    photoVerify();

                } else {
                    // ⚠️ No internet → show the same dialog you use in Splash/Login
                    showNoInternetDialog();
                }

            }
        });
        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DateFormat dfTime = new SimpleDateFormat("HH:mm");
                time = dfTime.format(Calendar.getInstance().getTime());
                try {
                    Date CurrentTime = dfTime.parse(time);
                    Date startTime = dfTime.parse("6:30");
                    Date endTime = dfTime.parse("8:01");
                    if (((CurrentTime.after(startTime)) || (CurrentTime.equals(startTime)))
                            && (CurrentTime.before(endTime) || (CurrentTime.equals(startTime)))) {
                        getLoc();
                        uploadData();
                    } else {
                        TimeAlert();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void TimeAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.lite_red);
        sDialog.setCustomImage(R.drawable.tick);
        sDialog.setContentText("Cancel");
        sDialog.setContentText("punching is only permitted between 06:30 and 8:00.");
        sDialog.setCancelable(false);
        sDialog.show();
    }
    private void getLoc() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return;
        } else {
            getLocation();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        //   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f / Math.PI);
        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return 6366000 * tt;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            GpsTracker gps = new GpsTracker(getApplicationContext());
            CurrentLat = String.valueOf(gps.getLatitude());
            CurrentLong = String.valueOf(gps.getLongitude());
            lat2 = Float.parseFloat(CurrentLat);
            long2 = Float.parseFloat(CurrentLong);
            //getAddressFromLatLng(lat2, long2);
            Double meter = meterDistanceBetweenPoints(GeoLat, GeoLong, lat2, long2);
            if (meter > 100.0) {
                binding.linear2.setVisibility(View.GONE);
//                LayoutInflater inflater = getLayoutInflater();
//                View layout = inflater.inflate(R.layout.custom_snackbar, null);
//                TextView text = layout.findViewById(R.id.toast_text);
//                text.setText("Outside of are" + selectedCenterId);
//                ImageView icon = layout.findViewById(R.id.toast_icon);
//                icon.setImageResource(R.drawable.tick); // Replace with your drawable
//                Toast toast = new Toast(getApplicationContext());
//                toast.setDuration(Toast.LENGTH_LONG);
//                toast.setView(layout);
//                toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
//                toast.show();
                Toast.makeText(this, "outside of area", Toast.LENGTH_SHORT).show();
                binding.linear3.setVisibility(View.GONE);
                //    Toast.makeText(MainActivity.this, "getLocation: " + CurrentLat + "long" + CurrentLong, Toast.LENGTH_LONG).show();
                CenterAlert();
            } else if (lat2 == 0.0 || long2 == 0.0) {
                RequestForGpsPermission();
//                LayoutInflater inflater = getLayoutInflater();
//                View layout = inflater.inflate(R.layout.custom_snackbar, null);
//                TextView text = layout.findViewById(R.id.toast_text);
//                text.setText("Turn on Gps" + selectedCenterId);
//                ImageView icon = layout.findViewById(R.id.toast_icon);
//                icon.setImageResource(R.drawable.tick); // Replace with your drawable
//                Toast toast = new Toast(getApplicationContext());
//                toast.setDuration(Toast.LENGTH_LONG);
//                toast.setView(layout);
//                toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
//                toast.show();
                Toast.makeText(this, "Turn on Gps", Toast.LENGTH_SHORT).show();
            } else

            {
                //  binding.linear2.setVisibility(View.VISIBLE);
                binding.linear3.setVisibility(View.VISIBLE);
                binding.submit.setVisibility(View.GONE);
            }
        }
    }

    private void CenterAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setTitleText("Outside Authorized Area");
        sDialog.setContentText("You are not within the authorized 100-meter radius.\nDo you want to change the center location?");
        sDialog.setCancelable(false);

        sDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                FDA_Request(CenterId);
            }
        });
        sDialog.setCancelButtonBackgroundColor(R.color.LightBlue);
        sDialog.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });

        sDialog.show();
    }

    private void navigateToChangeRequest() {
        String BranchId = String.valueOf(binding.BranchName.getText());
        Intent intent = new Intent(MainActivity.this, Location_Change_Request.class);
        intent.putExtra("ActivityName", "MainActivity");
        intent.putExtra("selectedItem", selectedItem);
        intent.putExtra("BranchId", BranchId);
        intent.putExtra("GeoLat", GeoLat);
        intent.putExtra("GeoLong", GeoLong);
        intent.putExtra("CenterId", CenterId);
        startActivity(intent);
    }

    private void FDA_Request(String value) {
        showLoader("");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("LocationModAlert");
        punchRequest.setEmpCode(EmpCode+"¥"+value);
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                punchResponse = response.body();
                hideLoader();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    Alert= punchResponse.getCenterpunchdata().get(0).getAlert();
                    if ("2".equals(Alert)) {
                        navigateToChangeRequest();
                    } else {
                        showAlertAlready();                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    hideLoader();
                }
            }

            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(MainActivity.this, "Network request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAlertAlready() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("Location already updated");
        sDialog.setConfirmButton("OK", null);
        sDialog.setCancelable(false);
        sDialog.show();
    }

    private void showMvtAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("You Already Updated");
        sDialog.setCancelable(false);
        sDialog.show();
        sDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onBackPressed();
            }
        });
    }

    private void getAddressFromLatLng(float latitude, float longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                city = address.getLocality(); // City name
                Toast.makeText(this, "" + city, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get address for this location.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void RequestForGpsPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            getLocation();
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            displayNeverAskAgainDialog();
                        } else {
                            Toast.makeText(MainActivity.this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown
                            (List<PermissionRequest> permissions, PermissionToken token) {
                        buildAlertMessageNoGps();
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void displayNeverAskAgainDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("We need to get device id for performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }

    private void getCenterList() {
        startTimeout(60000); // 1-minute timeout
        showLoader("");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("CenterList");
        punchRequest.setEmpCode(String.valueOf(EmpCode));
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                cancelTimeout();
                punchResponse = response.body();
                hideLoader();
                binding.adapterlayout.setVisibility(View.VISIBLE);
                ListNames.clear();
                CenterNames.clear();
                CenterNames.add("Select Center Name");
                centerMap.put("Select Center Name", "");

                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    //   CenterNames.add("Select Center Name");
                    for (int i = 0; i < punchResponse.getCenterpunchdata().size(); i++) {
                        Center = String.valueOf(punchResponse.getCenterpunchdata().get(i).getCenteRNAME());
                        CentId=String.valueOf(punchResponse.getCenterpunchdata().get(i).getCenteRID());
                        if (Center != null && !Center.isEmpty()) {
                            CenterNames.add(Center);
                            centerMap.put(Center, CentId);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.custom_spinner_item, CenterNames);
                    adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                    binding.CenterName.setAdapter(adapter);
                } else {
                    //  binding.adapterlayout.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "No center data available", Toast.LENGTH_SHORT).show();
                    hideLoader();
                }
            }
            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                cancelTimeout();
                hideLoader();
                onBackPressed();
            }
        });
    }

    private void GetCenterLocation(String Center,String CentId) {
        showLoader("Getting center location please wait....");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("geo_loc");
        punchRequest.setEmpCode(EmpCode + "¥" + Center+"¥"+CentId);
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                ListNames.clear();
                punchResponse = response.body();
                hideLoader();
                if ( punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    GeoLat = Float.parseFloat(punchResponse.getCenterpunchdata().get(0).getGeOLAT());
                    GeoLong = Float.parseFloat(punchResponse.getCenterpunchdata().get(0).getGeOLONG());
                    CenterId=punchResponse.getCenterpunchdata().get(0).getCenteRID();
                    //   getAddressFromLatLng(GeoLat, GeoLong);
                    //  Toast.makeText(MainActivity.this, "CENTER" + GeoLat + "LONG" + GeoLong, Toast.LENGTH_SHORT).show();
                    getLoc();
                } else {
                    //  binding.adapterlayout.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
                    hideLoader();
                }
            }
            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                hideLoader();
                onBackPressed();
            }
        });
    }

    private void getFDAList() {
        showLoader("");
        startTimeout(60000); // 1-minute timeout
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("FDAList");
        punchRequest.setEmpCode(EmpCode);
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                cancelTimeout();
                punchResponse = response.body();
                hideLoader();
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    binding.EmpCode.setText(String.valueOf(EmpCode));
                    binding.EmpName.setText(String.valueOf(punchResponse.getCenterpunchdata().get(0).getEmPNAME()));
                    binding.BranchName.setText(String.valueOf(punchResponse.getCenterpunchdata().get(0).getBrancHID()));
                } else {
                    Toast.makeText(MainActivity.this, "Network request failed", Toast.LENGTH_LONG).show();
                    hideLoader();
                }
            }
            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                cancelTimeout();
                hideLoader();
                onBackPressed();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CAME_REQ) {
            Bitmap camBitmap = null;
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                            bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                                decoder.setMutableRequired(true); // <-- here
                            });
//                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), imageUri));
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        }
                        Bitmap compressed = compressBitmapBelow500KB(bitmap);
                        base64Image = encodeToBase64(compressed);
                        camBitmap = bitmap;
                        Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else if (data.getExtras() != null) {
                    camBitmap = (Bitmap) data.getExtras().get("data");
                    if (camBitmap != null) {
                        Bitmap compressed = compressBitmapBelow500KB(camBitmap);
                        base64Image = encodeToBase64(compressed);
                    } else {
                        Toast.makeText(this, "Failed to retrieve the image.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, "No image data found!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (camBitmap != null) {
                    binding.imageView.setImageBitmap(camBitmap);
                    binding.imageView.setVisibility(View.VISIBLE);
                    binding.location.setVisibility(View.VISIBLE);
                    filePath = saveBitmapToFile(camBitmap);
                    file = new File(filePath);
                    fileName = file.getName();
                }
            } else {
                Toast.makeText(this, "Image selection failed or canceled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //    private Bitmap compressBitmapBelow500KB(Bitmap originalBitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        int quality = 100;
//        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//        while (outputStream.toByteArray().length > 500 * 1024 && quality > 10) {
//            outputStream.reset();
//            quality -= 5;
//            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//        }
//        byte[] byteArray = outputStream.toByteArray();
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//    }
    private Bitmap compressBitmapBelow500KB(Bitmap originalBitmap) {
        int maxFileSize = 500 * 1024; // 500 KB
        int quality = 100;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        // If it's already below 500KB, return it
        if (outputStream.toByteArray().length <= maxFileSize) {
            return originalBitmap;
        }
        // Resize if initial compression is not enough
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        while (outputStream.toByteArray().length > maxFileSize && quality > 10) {
            outputStream.reset();
            quality -= 5;

            // Reduce dimensions gradually
            width = (int) (width * 0.9);
            height = (int) (height * 0.9);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        byte[] byteArray = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private Bitmap compressBitmapTo3000KB(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float aspectRatio = (float) width / height;
        int newWidth = 5000;
        int newHeight = Math.round(newWidth / aspectRatio);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int quality = 95;
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        while (byteArrayOutputStream.size() < 3000 * 1024) {
            byteArrayOutputStream.reset();
            quality += 5;
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            if (quality >= 100) break;
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraImages");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File imageFile = new File(directory, System.currentTimeMillis() + ".JPEG");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("saveBitmapToFile", "Error saving bitmap to file", e);
            return null;
        }
    }

    private void uploadData() {
        if (base64Image == null) {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
        }
        if (binding.imageView.getDrawable() == null) {
            showAlertDialog();
            return;
        }
        DateFormat dfTime = new SimpleDateFormat("HH:mm");
        time = dfTime.format(Calendar.getInstance().getTime());
        val = "PUNCH"+"¥"+binding.BranchName.getText()+"¥"+EmpName+"¥"+selectedItem+"¥"+GeoLat+"¥"+GeoLong+
                "¥"+time+"¥"+lat2+"¥"+long2+"¥"+EmpCode+"¥"+CenterId;
        val1="PUNCH_MAIN"+"¥"+EmpCode+"¥"+binding.BranchName.getText()+"¥"+time+"¥"+lat2+"¥"+long2;
        //  Toast.makeText(MainActivity.this, ""+base64Image, Toast.LENGTH_SHORT).show();
        //  Log.e("Upload", ": " + base64Image);
        // sendToServer(val, base64Image);
        getPunch(base64Image);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Image")
                .setMessage("Please capture or select an image.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendToServer(String imageBase64) {
        showLoader("Saving images please wait ....");
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setpParameters(val);
        uploadRequest.setImageByte(imageBase64);
        Call<UploadResponse> call = RetrofitClient.getInstance().getMyApi().photoUpload(uploadRequest);
        call.enqueue(new Callback<UploadResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                uploadResponse = response.body();
                hideLoader();
                if (response.isSuccessful() && response.body() != null) {
                    String result = uploadResponse.getResult();
                    if (result != null && result.equals("Successfully Uploaded")) {
                        // getPunch(imageBase64);
                        showSuccessAlert();

                    } else if(result != null && result.equals("Already punch detected")) {
                        Toast.makeText(MainActivity.this, "Already punch", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("Upload", "Server Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("Upload", "Error reading errorBody: " + e.getMessage());
                    }
                    Toast.makeText(MainActivity.this, "Upload failed!!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                hideLoader();
                Log.e("Upload", "Error: " + t.getMessage());
            }
        });
    }

    private void showSuccessAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sDialog.setTitleText("Confirm");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setCustomImage(R.drawable.success);
        sDialog.setContentText("Punching Successfully");
        sDialog.setCancelable(false);
        sDialog.show();
        sDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitAlert();
    }

    private void showExitAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.custom_logout, null);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Optional: make background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnYes.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_snackbar, null);
            TextView text = layout.findViewById(R.id.toast_text);
            text.setText("Logout Successfully!");

            ImageView icon = layout.findViewById(R.id.toast_icon);
            icon.setImageResource(R.drawable.tick); // Replace with your drawable

            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.setGravity(Gravity.BOTTOM, 0, 100); // Optional: position of toast
            toast.show();
          //  Toast.makeText(MainActivity.this, "Logout successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginPage.class));
            finish();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void getPunch(String imageBase64) {
        //main punch
        showLoader("Punching please wait ...");
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setpParameters(val1);
        uploadRequest.setImageByte(imageBase64);
        Call<UploadResponse> call = RetrofitClient.getInstance().getMyApi().photoUpload(uploadRequest);
        call.enqueue(new Callback<UploadResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                uploadResponse = response.body();
                hideLoader();
                if (response.isSuccessful() && response.body() != null) {

                    String result = uploadResponse.getResult();
                    if (result != null && result.equals("Successfully Uploaded")) {
                        //  saveImageToRoom(imageBase64);
                        sendToServer(imageBase64);
                        // showSuccessAlert();
                    } else if(result != null && result.equals("Already punch detected")) {
                        Toast.makeText(MainActivity.this, "Already punch", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    hideLoader();
                    Toast.makeText(MainActivity.this, "Upload failed!!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                hideLoader();
                onBackPressed();
            }
        });
    }




    private void photoVerify() {
        showLoader("Checking photos");
        PhotoVerificationRequest photoVerificationRequest = new PhotoVerificationRequest();
        photoVerificationRequest.setLanguageCode("en"); // example
        photoVerificationRequest.setHideBottomLogo("true");
        // photoVerificationRequest.setMatchImage("https://amfluat.asirvad.com/AssetManage/content/img/359868.png"); // your selfie or ID image in Base64
        photoVerificationRequest.setAccentColor("#00FF00"); // optional
        photoVerificationRequest.setAdditionalChecks("true");
        photoVerificationRequest.setAllowCameraSwitch("true");
        photoVerificationRequest.setFaceMatchThreshold("0.8");
        List<String> matchImages = new ArrayList<>();
        matchImages.add("https://amfluat.asirvad.com/AssetManage/content/img/330011.jpg");
        photoVerificationRequest.setMatchImage(matchImages);
        Call<PhotoVerificationResponse> call = RetrofitClientPhotoVerify.getInstance().getMyApi().photoVerification(photoVerificationRequest);
        call.enqueue(new Callback<PhotoVerificationResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PhotoVerificationResponse> call, Response<PhotoVerificationResponse> response) {
                photoVerificationResponse = response.body();
                hideLoader();
                if (response.isSuccessful() && response.body() != null) {
                    String getUrl = photoVerificationResponse.getVideoUrl();
                    String getToken=photoVerificationResponse.getToken();
                    String additionalCheck= photoVerificationResponse.getAdditionalChecks();
                    String faceMatch= String.valueOf(photoVerificationResponse.getMatchImage());
                    String flipCamera=photoVerificationResponse.getAllowCameraSwitch();
                    Log.d("getURl","getURl got" +getUrl);
                    if (getUrl != null && !getUrl.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                        intent.putExtra("videoUrl", getUrl);
                        intent.putExtra("token",getToken);
                        intent.putExtra("additionalCheck",additionalCheck);
                        intent.putExtra("faceMatch",faceMatch);
                        intent.putExtra("flipCamera",flipCamera);
                        //   verifyPhotoComplete(getToken);
                        startActivity(intent);
                        //  Toast.makeText(MainActivity.this, "Upload Success!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Video Url not available!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("Upload", "Server Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        //  Log.e("Upload", "Error reading errorBody: " + e.getMessage());
                    }
                    Toast.makeText(MainActivity.this, "Upload failed!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PhotoVerificationResponse> call, Throwable t) {
                hideLoader();
                //  Log.e("Upload", "Error: " + t.getMessage());
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

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedImage = prefs.getString("capturedImage", null);
        boolean verifiedThisSession = prefs.getBoolean("verifiedThisSession", false);
        boolean verificationFailed = prefs.getBoolean("verificationFailed", false);
        if (verifiedThisSession && savedImage != null && !savedImage.isEmpty()) {
            Log.d("savedImage", "Loaded verified image in onResume: " + savedImage);
            binding.imageView.setVisibility(View.VISIBLE);
            binding.submit.setVisibility(View.VISIBLE);
            binding.linear2.setVisibility(View.VISIBLE);
            binding.photoVerify.setVisibility(View.GONE);
            binding.tvSuccess.setVisibility(View.VISIBLE);
            binding.tvSuccess.setText("Verified Successfully");
            binding.ivSuccessTick.setVisibility(View.VISIBLE);
            binding.tvSuccess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.light_green));
            binding.ivSuccessTick.setImageResource(R.drawable.tick);
            Glide.with(MainActivity.this).load(savedImage).into(binding.imageView);
        } else if (verificationFailed) {
            // ❌ Verification failed — ask again
            binding.imageView.setVisibility(View.GONE);
            binding.submit.setVisibility(View.GONE);
            binding.linear2.setVisibility(View.GONE);
            binding.photoVerify.setVisibility(View.VISIBLE);
            binding.tvSuccess.setVisibility(View.VISIBLE);
            binding.tvSuccess.setText("Verification Failed");
            binding.ivSuccessTick.setVisibility(View.VISIBLE);
            binding.tvSuccess.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red));
            binding.ivSuccessTick.setImageResource(R.drawable.error);
            Log.d("verification", "Verification failed — showing verify button again");

        } else {
            // 🧹 Fresh state — nothing verified yet
            binding.imageView.setVisibility(View.GONE);
            binding.submit.setVisibility(View.GONE);
            binding.linear2.setVisibility(View.GONE);
            binding.photoVerify.setVisibility(View.VISIBLE);
            binding.tvSuccess.setVisibility(View.GONE);
            binding.ivSuccessTick.setVisibility(View.GONE);
        }
    }
}