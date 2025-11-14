package com.example.centerpunch.PhotoVerificationCompleteApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FaceMatch {

    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("matchPercentage")
    @Expose
    private String matchPercentage;

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(String matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

}
