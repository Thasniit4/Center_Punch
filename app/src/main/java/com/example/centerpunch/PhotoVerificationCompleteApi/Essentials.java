package com.example.centerpunch.PhotoVerificationCompleteApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Essentials {

    @SerializedName("matchImage")
    @Expose
    private List<String> matchImage;
    @SerializedName("languageCode")
    @Expose
    private String languageCode;
    @SerializedName("accentColor")
    @Expose
    private String accentColor;
    @SerializedName("hideBottomLogo")
    @Expose
    private String hideBottomLogo;
    @SerializedName("backgroundColor")
    @Expose
    private String backgroundColor;
    @SerializedName("piiDeletionTTL")
    @Expose
    private String piiDeletionTTL;
    @SerializedName("allowCameraSwitch")
    @Expose
    private String allowCameraSwitch;
    @SerializedName("faceMatchThreshold")
    @Expose
    private Double faceMatchThreshold;

    public List<String> getMatchImage() {
        return matchImage;
    }

    public void setMatchImage(List<String> matchImage) {
        this.matchImage = matchImage;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getHideBottomLogo() {
        return hideBottomLogo;
    }

    public void setHideBottomLogo(String hideBottomLogo) {
        this.hideBottomLogo = hideBottomLogo;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getPiiDeletionTTL() {
        return piiDeletionTTL;
    }

    public void setPiiDeletionTTL(String piiDeletionTTL) {
        this.piiDeletionTTL = piiDeletionTTL;
    }

    public String getAllowCameraSwitch() {
        return allowCameraSwitch;
    }

    public void setAllowCameraSwitch(String allowCameraSwitch) {
        this.allowCameraSwitch = allowCameraSwitch;
    }

    public Double getFaceMatchThreshold() {
        return faceMatchThreshold;
    }

    public void setFaceMatchThreshold(Double faceMatchThreshold) {
        this.faceMatchThreshold = faceMatchThreshold;
    }

}
