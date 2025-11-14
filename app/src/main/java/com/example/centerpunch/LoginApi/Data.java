package com.example.centerpunch.LoginApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data  {
    @SerializedName("isDataAvailable")
    @Expose
    private Boolean isDataAvailable;
    @SerializedName("userId")
    @Expose
    private Integer userId;
    @SerializedName("accessLevelId")
    @Expose
    private Integer accessLevelId;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("branchId")
    @Expose
    private Integer branchId;
    @SerializedName("branchName")
    @Expose
    private String branchName;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("mpin")
    @Expose
    private Integer mpin;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("accessLevelName")
    @Expose
    private String accessLevelName;

    public Boolean getIsDataAvailable() {
        return isDataAvailable;
    }

    public void setIsDataAvailable(Boolean isDataAvailable) {
        this.isDataAvailable = isDataAvailable;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAccessLevelId() {
        return accessLevelId;
    }

    public void setAccessLevelId(Integer accessLevelId) {
        this.accessLevelId = accessLevelId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getMpin() {
        return mpin;
    }

    public void setMpin(Integer mpin) {
        this.mpin = mpin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccessLevelName() {
        return accessLevelName;
    }

    public void setAccessLevelName(String accessLevelName) {
        this.accessLevelName = accessLevelName;
    }

}

