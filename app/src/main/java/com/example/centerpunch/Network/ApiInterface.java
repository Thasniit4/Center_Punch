package com.example.centerpunch.Network;


import com.example.centerpunch.LoginApi.LoginRequest;
import com.example.centerpunch.LoginApi.LoginResponse;
import com.example.centerpunch.LoginApi.PhotoExistRequest;
import com.example.centerpunch.LoginApi.PhotoExistResponse;
import com.example.centerpunch.PhotoVerificationApi.PhotoVerificationRequest;
import com.example.centerpunch.PhotoVerificationApi.PhotoVerificationResponse;
import com.example.centerpunch.PhotoVerificationCompleteApi.VerificationCompleteRequest;
import com.example.centerpunch.PhotoVerificationCompleteApi.VerificationCompleteResponse;
import com.example.centerpunch.PunchApi.PunchRequest;
import com.example.centerpunch.PunchApi.PunchResponse;
import com.example.centerpunch.UploadPhoto.UploadRequest;
import com.example.centerpunch.UploadPhoto.UploadResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    @POST("api/hrmslogin/hrmslogin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/CenterPunch/getCenterPunchDatas")
    Call<PunchResponse> punch(@Body PunchRequest punchRequest);

    @POST("api/CenterPunchPhoto/getPhotoPunchDatas")
    Call<UploadResponse> photoUpload(@Body UploadRequest uploadRequest);

    @POST("liveness-secure/createUrl")
    Call<PhotoVerificationResponse> photoVerification(@Body PhotoVerificationRequest photoVerificationRequest);

    @POST("liveness-secure/getData")
    Call<VerificationCompleteResponse> verifyPhotoCompletion(@Body VerificationCompleteRequest verificationCompleteRequest);

    @POST("api/ImageUpload/getImageUploadDatas")
    Call<PhotoExistResponse> checkingPhotoExist(@Body PhotoExistRequest photoExistRequest);


}
