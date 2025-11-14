package com.example.centerpunch.PhotoVerificationCompleteApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("consumerId")
    @Expose
    private String consumerId;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("isUsed")
    @Expose
    private Integer isUsed;
    @SerializedName("capturedImage")
    @Expose
    private String capturedImage;
    @SerializedName("passiveLiveliness")
    @Expose
    private PassiveLiveliness passiveLiveliness;
    @SerializedName("faceMatch")
    @Expose
    private FaceMatch faceMatch;
    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("additionalChecks")
    @Expose
    private AdditionalChecks additionalChecks;

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Integer isUsed) {
        this.isUsed = isUsed;
    }

    public String getCapturedImage() {
        return capturedImage;
    }

    public void setCapturedImage(String capturedImage) {
        this.capturedImage = capturedImage;
    }

    public PassiveLiveliness getPassiveLiveliness() {
        return passiveLiveliness;
    }

    public void setPassiveLiveliness(PassiveLiveliness passiveLiveliness) {
        this.passiveLiveliness = passiveLiveliness;
    }

    public FaceMatch getFaceMatch() {
        return faceMatch;
    }

    public void setFaceMatch(FaceMatch faceMatch) {
        this.faceMatch = faceMatch;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public AdditionalChecks getAdditionalChecks() {
        return additionalChecks;
    }

    public void setAdditionalChecks(AdditionalChecks additionalChecks) {
        this.additionalChecks = additionalChecks;
    }

}
