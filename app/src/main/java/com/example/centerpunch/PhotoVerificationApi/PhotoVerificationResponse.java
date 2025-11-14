package com.example.centerpunch.PhotoVerificationApi;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class PhotoVerificationResponse {

    @SerializedName("languageCode")
    @Expose
    private String languageCode;
    @SerializedName("matchImage")
    @Expose
    private List<String> matchImage;
    @SerializedName("hideBottomLogo")
    @Expose
    private String hideBottomLogo;
    @SerializedName("accentColor")
    @Expose
    private String accentColor;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("consumerId")
    @Expose
    private String consumerId;
    @SerializedName("videoUrl")
    @Expose
    private String videoUrl;
    @SerializedName("additionalChecks")
    @Expose
    private String additionalChecks;
    @SerializedName("reviewImage")
    @Expose
    private String reviewImage;
    @SerializedName("backgroundColor")
    @Expose
    private String backgroundColor;
    @SerializedName("faceMatchThreshold")
    @Expose
    private Double faceMatchThreshold;
    @SerializedName("piiDeletionTTL")
    @Expose
    private String piiDeletionTTL;
    @SerializedName("allowCameraSwitch")
    @Expose
    private String allowCameraSwitch;

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public List<String> getMatchImage() {
        return matchImage;
    }

    public void setMatchImage(List<String> matchImage) {
        this.matchImage = matchImage;
    }

    public String getHideBottomLogo() {
        return hideBottomLogo;
    }

    public void setHideBottomLogo(String hideBottomLogo) {
        this.hideBottomLogo = hideBottomLogo;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAdditionalChecks() {
        return additionalChecks;
    }

    public void setAdditionalChecks(String additionalChecks) {
        this.additionalChecks = additionalChecks;
    }

    public String getReviewImage() {
        return reviewImage;
    }

    public void setReviewImage(String reviewImage) {
        this.reviewImage = reviewImage;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Double getFaceMatchThreshold() {
        return faceMatchThreshold;
    }

    public void setFaceMatchThreshold(Double faceMatchThreshold) {
        this.faceMatchThreshold = faceMatchThreshold;
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

}
