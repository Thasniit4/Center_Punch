package com.example.centerpunch.PhotoVerificationCompleteApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdditionalChecks {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("attemptNumber")
    @Expose
    private Integer attemptNumber;
    @SerializedName("failedChecks")
    @Expose
    private List<Object> failedChecks;
    @SerializedName("isFaceCovered")
    @Expose
    private Boolean isFaceCovered;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public List<Object> getFailedChecks() {
        return failedChecks;
    }

    public void setFailedChecks(List<Object> failedChecks) {
        this.failedChecks = failedChecks;
    }

    public Boolean getIsFaceCovered() {
        return isFaceCovered;
    }

    public void setIsFaceCovered(Boolean isFaceCovered) {
        this.isFaceCovered = isFaceCovered;
    }

}
