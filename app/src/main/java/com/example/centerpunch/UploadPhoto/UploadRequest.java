package com.example.centerpunch.UploadPhoto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadRequest {
    @SerializedName("p_parameters")
    @Expose
    private String pParameters;
    @SerializedName("imageByte")
    @Expose
    private String imageByte;

    public String getpParameters() {
        return pParameters;
    }

    public void setpParameters(String pParameters) {
        this.pParameters = pParameters;
    }

    public String getImageByte() {
        return imageByte;
    }

    public void setImageByte(String imageByte) {
        this.imageByte = imageByte;
    }

}