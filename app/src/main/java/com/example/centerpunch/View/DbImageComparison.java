package com.example.centerpunch.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.centerpunch.BaseMethod.BaseActivity;
import com.example.centerpunch.Network.NetWorkCheck;
import com.example.centerpunch.PunchApi.RetrofitClient;
import com.example.centerpunch.R;
import com.example.centerpunch.UploadPhoto.UploadRequest;
import com.example.centerpunch.UploadPhoto.UploadResponse;
import com.example.centerpunch.databinding.ActivityDbImageComparisonBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DbImageComparison extends BaseActivity implements NetWorkCheck.NetworkChangeListener {

    private androidx.appcompat.app.AlertDialog loaderDialog;
    File file;
    String uploadPhoto, checkPhoto;
    ActivityDbImageComparisonBinding binding;
    UploadResponse uploadResponse;
    String time;
    private String base64Image = null;
    private static final int CAME_REQ = 100;
    String filePath,fileName;
    private NetWorkCheck netWorkCheck;
    private AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDbImageComparisonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NetWorkCheck.setNetworkChangeListener(this);
        NetWorkCheck.registerNetworkCallback(this);
        if (!NetWorkCheck.isInternetAvailable(this)) {
            showNoInternetDialog();
        }
        // Open camera
        binding.cameraButton.setOnClickListener(v -> {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (camIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(camIntent, CAME_REQ);
            }
        });

        // Submit button
        binding.submit.setOnClickListener(v -> uploadData());
    }

    /**
     * Validates image and uploads data.
     */
    private void uploadData() {
        if (binding.imageView.getDrawable() == null) {
            showAlertDialog();
            return;
        }

        if (base64Image == null) {
            Toast.makeText(this, "Please capture or select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare upload data
        DateFormat dfTime = new SimpleDateFormat("HH:mm");
        time = dfTime.format(Calendar.getInstance().getTime());
        checkPhoto = EmpCode + "¥";
        uploadPhoto = "PHOTO_UPLOAD" + "¥" + EmpCode;


        getPunch(base64Image);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.capture_image, null);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        // Optional: make background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
//        btnYes.setOnClickListener(v -> {
//            finish();
////            Toast.makeText(DbImageComparison.this, "Logout successfully", Toast.LENGTH_SHORT).show();
////            startActivity(new Intent(MainActivity.this, LoginPage.class));
////            finish();
//            dialog.dismiss();
//        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Alert shown if no image is selected.
     */
//    private void showAlertDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("No Image")
//                .setMessage("Please capture or select an image.")
//                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
//                .show();
//    }

    /**
     * Upload image to server using Retrofit.
     */
    private void getPunch(String imageBase64) {
        showLoader("Loading");
        UploadRequest uploadRequest = new UploadRequest();
        uploadRequest.setpParameters(uploadPhoto);
        uploadRequest.setImageByte(imageBase64);

        Call<UploadResponse> call = RetrofitClient.getInstance().getMyApi().photoUpload(uploadRequest);
        call.enqueue(new Callback<UploadResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
               hideLoader();
                if (response.isSuccessful() && response.body() != null) {
                    uploadResponse = response.body();
                    String result = uploadResponse.getResult();
                    if ("Successfully Uploaded".equals(result)) {
                        showSuccessAlert();
                    } else {
                        Toast.makeText(DbImageComparison.this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DbImageComparison.this, "Upload failed!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(DbImageComparison.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show success dialog and navigate to MainActivity on OK click.
     */
    private void showSuccessAlert() {
        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sDialog.setTitleText("Confirm");
        sDialog.setContentText("Punching Successfully");
        sDialog.setCustomImage(R.drawable.tick);
        sDialog.setCancelable(false);

        sDialog.setConfirmButton("OK", sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            Intent intent = new Intent(DbImageComparison.this, DashBoardActivity.class);
            startActivity(intent);
            finish();
        });

        sDialog.show();

        // Set confirm button color after show
        sDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.LightBlue));
    }

    /**
     * Handle camera result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAME_REQ && data != null) {
            Bitmap camBitmap = null;

            try {
                if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                        bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> decoder.setMutableRequired(true));
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    }
                    camBitmap = compressBitmapBelow500KB(bitmap);
                } else if (data.getExtras() != null) {
                    camBitmap = (Bitmap) data.getExtras().get("data");
                    if (camBitmap != null) {
                        camBitmap = compressBitmapBelow500KB(camBitmap);
                    }
                }

                if (camBitmap != null) {
                    base64Image = encodeToBase64(camBitmap);
                    binding.imageView.setImageBitmap(camBitmap);
                    binding.imageView.setVisibility(View.VISIBLE);

                    filePath = saveBitmapToFile(camBitmap);
                    file = new File(filePath);
                    fileName = file.getName();

                    Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No image data found!", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap compressBitmapBelow500KB(Bitmap originalBitmap) {
        int maxFileSize = 1024 * 1024; // 500 KB
        int quality = 95; // start high
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Step 1: Compress at high quality first
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        // Step 2: If still large, slightly reduce quality only (avoid shrinking dimensions immediately)
        while (outputStream.toByteArray().length > maxFileSize && quality > 60) {
            outputStream.reset();
            quality -= 5;
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        // Step 3: If still too big, then scale down dimensions gradually
        if (outputStream.toByteArray().length > maxFileSize) {
            double ratio = Math.sqrt((double) maxFileSize / outputStream.toByteArray().length);
            int newWidth = (int) (originalBitmap.getWidth() * ratio);
            int newHeight = (int) (originalBitmap.getHeight() * ratio);

            Bitmap resized = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            outputStream.reset();
            quality = 90; // reset to higher quality for final encode
            resized.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        byte[] byteArray = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }



    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraImages");
            if (!directory.exists()) directory.mkdirs();

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

    /**
     * Encode bitmap to Base64 string.
     */
    private String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
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

