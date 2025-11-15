package com.example.centerpunch.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.BaseMethod.GpsTracker;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PunchApi.PunchRequest;
import com.example.centerpunch.PunchApi.PunchResponse;
import com.example.centerpunch.PunchApi.RetrofitClient;
import com.example.centerpunch.R;
import com.example.centerpunch.UploadPhoto.UploadRequest;
import com.example.centerpunch.UploadPhoto.UploadResponse;
import com.example.centerpunch.databinding.ActivityRitcRtsePunchBinding;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RITC_RTSE_PUNCH extends BaseActivity implements NetWorkCheck.NetworkChangeListener {
    private androidx.appcompat.app.AlertDialog noInternetDialog;
    private androidx.appcompat.app.AlertDialog loaderDialog;
    private static final int REQUEST_LOCATION = 1;
    private static final int CAME_REQ = 100;
    private String base64Image = null;
    File file;
    String filePath,fileName,Loc;
    ActivityRitcRtsePunchBinding binding;
    LocationManager locationManager;
    private String selectedItem;
    PunchResponse punchResponse;
    List<String> BranchNames = new ArrayList<String>();
    HashMap<String, String> branchMap = new HashMap<>();
    Float GeoLat,GeoLong,lat2,long2;
    String BranchName, city,val,time,val1,CurrentLat,CurrentLong,BranchId,CenterId,Alert,BrId,selectedBranchId;
    private ArrayList<String> ListNames = new ArrayList<String>();
    private Handler sessionHandler;
    private Runnable sessionRunnable;
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000;
    UploadResponse uploadResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ritc_rtse_punch);
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        binding = ActivityRitcRtsePunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!NetWorkCheck.isInternetAvailable()) {
            showNoInternetDialog();
        }
        sessionHandler = new Handler();
        sessionRunnable = () -> {
            Toast.makeText(RITC_RTSE_PUNCH.this, "Session expired due to inactivity", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(RITC_RTSE_PUNCH.this, LoginPage.class);
            startActivity(intent);
            finish();
        };
        resetSessionTimer();
        binding.linear2.setVisibility(View.GONE);
        binding.linear3.setVisibility(View.GONE);
        binding.tvBranchId.setVisibility(View.GONE);
        binding.BranchId.setVisibility(View.GONE);
        RITC_RTSEList();
        //      getBranchList();
        binding.BranchName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Select Branch Name")) {
                    binding.imageView.setImageDrawable(null);
                    base64Image = "";
                    selectedBranchId="";
                    binding.tvBranchId.setVisibility(View.GONE);
                    binding.BranchId.setVisibility(View.GONE);
                    binding.submit.setVisibility(View.GONE);
                    binding.linear2.setVisibility(View.GONE);
                } else {
                    binding.imageView.setImageDrawable(null);
                    binding.BranchId.setVisibility(View.VISIBLE);
                    binding.tvBranchId.setVisibility(View.VISIBLE);
                    binding.submit.setVisibility(View.VISIBLE);
                    selectedBranchId = branchMap.get(selectedItem);
                    binding.BranchId.setText(selectedBranchId);
                    //  Toast.makeText(RITC_RTSE_PUNCH.this, "Branch ID: " + selectedBranchId , Toast.LENGTH_LONG).show();
                    GetBranchLocation(selectedItem,selectedBranchId);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent CameIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (CameIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(CameIntent, CAME_REQ);
                }
            }
        });
        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoc();
                GetStatus();
                //  uploadData();
            }
        });
    }
    private void getLoc() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
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
        resetSessionTimer();
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
                RITC_RTSE_PUNCH.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                RITC_RTSE_PUNCH.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            GpsTracker gps = new GpsTracker(getApplicationContext());
            CurrentLat = String.valueOf(gps.getLatitude());
            CurrentLong = String.valueOf(gps.getLongitude());
            lat2 = Float.parseFloat(CurrentLat);
            long2 = Float.parseFloat(CurrentLong);
            if (lat2 == null || long2 == null || lat2 == 0.0 || long2 == 0.0) {
                RequestForGpsPermission();
                Toast.makeText(this, "Turn on Gps", Toast.LENGTH_SHORT).show();
                binding.linear2.setVisibility(View.GONE);
                binding.linear3.setVisibility(View.GONE);
                return;
            }
            //getAddressFromLatLng(lat2, long2);
            Double meter = meterDistanceBetweenPoints(GeoLat, GeoLong, lat2, long2);
            if (meter > 100.0) {
                binding.linear2.setVisibility(View.GONE);
                Toast.makeText(this, "outside of area", Toast.LENGTH_SHORT).show();
                binding.linear3.setVisibility(View.GONE);
                //    Toast.makeText(MainActivity.this, "getLocation: " + CurrentLat + "long" + CurrentLong, Toast.LENGTH_LONG).show();
                CenterAlert();
            } else if (lat2 == 0.0 || long2 == 0.0) {
                RequestForGpsPermission();
                Toast.makeText(this, "Turn on Gps", Toast.LENGTH_SHORT).show();
            } else {
                binding.linear2.setVisibility(View.VISIBLE);
                binding.linear3.setVisibility(View.VISIBLE);
            }
        }
    }

    private void CenterAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setTitleText("Outside Authorized Area");
        sDialog.setContentText("You are not within the authorized 100-meter radius.");
        sDialog.setCancelable(false);
        sDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);

        sDialog.show();
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
                            Toast.makeText(RITC_RTSE_PUNCH.this, "Location permission denied.", Toast.LENGTH_SHORT).show();
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
    @Override
    public void onResume() {
        super.onResume();
    }
    private void getBranchList(String Id) {
        startTimeout(60000); // 1-minute timeout
        showLoader("Getting branch list please wait.....");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("BranchList");
        punchRequest.setEmpCode(EmpCode+"¥"+Id);
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
                BranchNames.clear();
                binding.BranchId.setText("");
                BranchNames.add("Select Branch Name");
                branchMap.put("Select Branch Name", "");
                if (punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    for (int i = 0; i < punchResponse.getCenterpunchdata().size(); i++) {
                        BranchName = String.valueOf(punchResponse.getCenterpunchdata().get(i).getAlert());////mattanm
                        if (BranchName.equals("NOT")){
                            showAlert();
                        }else{
                            BrId=String.valueOf(punchResponse.getCenterpunchdata().get(i).getBrancHID());
                            if (BranchName != null && !BranchName.isEmpty()) {
                                BranchNames.add(BranchName);
                                branchMap.put(BranchName, BrId);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(RITC_RTSE_PUNCH.this, R.layout.custom_spinner_item, BranchNames);
                        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
                        binding.BranchName.setAdapter(adapter);
                    }} else {
                    //  binding.adapterlayout.setVisibility(View.GONE);
                    Toast.makeText(RITC_RTSE_PUNCH.this, "No branch data available", Toast.LENGTH_SHORT).show();
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
    private void GetBranchLocation(String Branch,String BrnId) {
        showLoader("Getting branch location please wait ...");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("Ritc_loc");
        punchRequest.setEmpCode(EmpCode + "¥" + Branch+"¥"+BrnId);
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                ListNames.clear();
                punchResponse = response.body();
                hideLoader();
                if ( punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    Loc =punchResponse.getCenterpunchdata().get(0).getAlert();
                    if (Loc.equals("0") || Loc.equals("NULL")) {
                        showLatLongErrorAlert("No valid location found (latitude and longitude are zero).");
                    }else{
                        splitAndDisplayLatLong(Loc);
                        //getAddressFromLatLng(GeoLat, GeoLong);//  Toast.makeText(MainActivity.this, "CENTER" + GeoLat + "LONG" + GeoLong, Toast.LENGTH_SHORT).show();
                        getLoc();}
                } else {
                    //  binding.adapterlayout.setVisibility(View.GONE);
                    Toast.makeText(RITC_RTSE_PUNCH.this, "An error has occurred", Toast.LENGTH_SHORT).show();
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
    private void RITC_RTSEList() {
        showLoader("");
        startTimeout(60000); // 1-minute timeout
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("RITC_RTSEList");
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
                    String Id= String.valueOf(punchResponse.getCenterpunchdata().get(0).getBrancHID());
                    getBranchList(Id);
                } else {
                    Toast.makeText(RITC_RTSE_PUNCH.this, "Network request failed", Toast.LENGTH_LONG).show();
                    hideLoader();
                }
            }
            @Override
            public void onFailure(Call<PunchResponse> call, Throwable t) {
                cancelTimeout();
                hideLoader();
                onBackPressed();
                Toast.makeText(RITC_RTSE_PUNCH.this, "Network request failed", Toast.LENGTH_LONG).show();
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
    private Bitmap compressBitmapBelow500KB(Bitmap originalBitmap) {
        int maxFileSize = 500 * 1024; // 500 KB
        int quality = 100;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        if (outputStream.toByteArray().length <= maxFileSize) {
            return originalBitmap;
        }
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        while (outputStream.toByteArray().length > maxFileSize && quality > 10) {
            outputStream.reset();
            quality -= 5;
            width = (int) (width * 0.9);
            height = (int) (height * 0.9);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }
        byte[] byteArray = outputStream.toByteArray();
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
        val = "RTSE_RITC"+"¥"+EmpCode+"¥"+selectedBranchId+"¥"+EmpName+"¥"+selectedItem+"¥"+GeoLat+"¥"+GeoLong+
                "¥"+time+"¥"+lat2+"¥"+long2;
        sendToServer( base64Image);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Image")
                .setMessage("Please capture or select an image.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendToServer(String imageBase64) {
        showLoader("");
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
                        Toast.makeText(RITC_RTSE_PUNCH.this, "Already punch", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RITC_RTSE_PUNCH.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("Upload", "Server Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e("Upload", "Error reading errorBody: " + e.getMessage());
                    }
                    Toast.makeText(RITC_RTSE_PUNCH.this, "Upload failed!!", Toast.LENGTH_SHORT).show();
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
        sDialog.setContentText("Successfully Uploaded");
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
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout from your account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RITC_RTSE_PUNCH.this, "Logout successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RITC_RTSE_PUNCH.this, LoginPage.class));
                        RITC_RTSE_PUNCH.this.finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
    private void splitAndDisplayLatLong(String latLongStr) {
        if (latLongStr.equals("0") || latLongStr.equals("NULL")) {
            showLatLongErrorAlert("No valid location found (latitude and longitude are zero).");
            return;
        }
        if (latLongStr != null && latLongStr.contains(",")) {
            String[] parts = latLongStr.split(",");
            if (parts.length == 2) {
                try {
                    GeoLat = Float.parseFloat(parts[0].trim());
                    GeoLong = Float.parseFloat(parts[1].trim());
                    if (GeoLat == 0.0f && GeoLong == 0.0f) {
                        showLatLongErrorAlert("No valid location found (latitude and longitude are zero).");
                        return;
                    }
                    String formattedLat = String.format(Locale.US, "%.6f", GeoLat);
                    String formattedLong = String.format(Locale.US, "%.6f", GeoLong);
                    Log.d("LatLong", "Latitude: " + formattedLat + ", Longitude: " + formattedLong);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    showLatLongErrorAlert("Invalid number format in latitude/longitude.");
                }
            } else {
                showLatLongErrorAlert("Latitude and Longitude must be separated by one comma.");
            }
        } else {
            showLatLongErrorAlert("Latitude/Longitude string is empty or malformed.");
        }
    }
    private void showLatLongErrorAlert(String message) {
        SweetAlertDialog alert = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        alert.setTitleText("Invalid Location");
        alert.setContentText(message);
        alert.setConfirmText("OK");
        alert.setCancelable(false);
        alert.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        alert.show();
    }
    private void GetStatus() {
        showLoader("");
        PunchRequest punchRequest = new PunchRequest();
        punchRequest.setpFlag("Ritc_status");
        punchRequest.setEmpCode(EmpCode+"¥"+selectedBranchId );
        Call<PunchResponse> call = RetrofitClient.getInstance().getMyApi().punch(punchRequest);
        call.enqueue(new Callback<PunchResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<PunchResponse> call, Response<PunchResponse> response) {
                punchResponse = response.body();
                hideLoader();
                if(punchResponse.getCenterpunchdata().size() > 0){
                    //    if ( punchResponse.getCenterpunchdata() != null && !punchResponse.getCenterpunchdata().isEmpty()) {
                    int count = Integer.parseInt(String.valueOf(punchResponse.getCenterpunchdata().get(0).getStatuSID()));
                    int count1 = Integer.parseInt(String.valueOf(punchResponse.getCenterpunchdata().get(0).getReqcount()));

                    if (count == 0 || count1==0) {
                        uploadData();
                    } else {
                        showMvtAlert();
                    }


                } else {
                    Toast.makeText(RITC_RTSE_PUNCH.this, "You have a punch out  is pending to close.You can't punch in  another branch", Toast.LENGTH_SHORT).show();
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

    private void showMvtAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("You have a punch out  is pending to close.You can't punch in  another branch");
        sDialog.setCancelable(false);
        sDialog.show();
    }

    private void showAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sDialog.setTitleText("Oops...");
        sDialog.setConfirmButtonBackgroundColor(R.color.LightBlue);
        sDialog.setContentText("No Data Available");
        sDialog.setCancelable(false);
        sDialog.show();
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

        noInternetDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
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