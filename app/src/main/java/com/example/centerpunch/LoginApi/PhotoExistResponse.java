package com.example.centerpunch.LoginApi;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoExistResponse {

    @SerializedName("m_photo")
    @Expose
    private String mPhoto;
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("emp_code")
    @Expose
    private Integer empCode;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;

    public String getmPhoto() {
        return mPhoto;
    }

    public void setmPhoto(String mPhoto) {
        this.mPhoto = mPhoto;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getEmpCode() {
        return empCode;
    }

    public void setEmpCode(Integer empCode) {
        this.empCode = empCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}