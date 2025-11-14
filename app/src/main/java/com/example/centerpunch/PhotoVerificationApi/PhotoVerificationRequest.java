package com.example.centerpunch.PhotoVerificationApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoVerificationRequest {

    @SerializedName("languageCode")
    @Expose
    private String languageCode;
    @SerializedName("matchImage")
    @Expose
    private List<String> matchImage;
    @SerializedName("hideBottomLogo")
    @Expose
    private String hideBottomLogo;
    @SerializedName("reviewImage")
    @Expose
    private String reviewImage;
    @SerializedName("accentColor")
    @Expose
    private String accentColor;
    @SerializedName("additionalChecks")
    @Expose
    private String additionalChecks;
    @SerializedName("allowCameraSwitch")
    @Expose
    private String allowCameraSwitch;
    @SerializedName("faceMatchThreshold")
    @Expose
    private String faceMatchThreshold;

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

    public String getReviewImage() {
        return reviewImage;
    }

    public void setReviewImage(String reviewImage) {
        this.reviewImage = reviewImage;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getAdditionalChecks() {
        return additionalChecks;
    }

    public void setAdditionalChecks(String additionalChecks) {
        this.additionalChecks = additionalChecks;
    }

    public String getAllowCameraSwitch() {
        return allowCameraSwitch;
    }

    public void setAllowCameraSwitch(String allowCameraSwitch) {
        this.allowCameraSwitch = allowCameraSwitch;
    }

    public String getFaceMatchThreshold() {
        return faceMatchThreshold;
    }

    public void setFaceMatchThreshold(String faceMatchThreshold) {
        this.faceMatchThreshold = faceMatchThreshold;
    }

}
