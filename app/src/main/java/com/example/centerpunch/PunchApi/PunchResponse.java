package com.example.centerpunch.PunchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PunchResponse {
    @SerializedName("isDataAvailable")
    @Expose
    private Boolean isDataAvailable;
    @SerializedName("centerpunchdata")
    @Expose
    private List<PunchData> centerpunchdata;

    public Boolean getIsDataAvailable() {
        return isDataAvailable;
    }

    public void setIsDataAvailable(Boolean isDataAvailable) {
        this.isDataAvailable = isDataAvailable;
    }

    public List<PunchData> getCenterpunchdata() {
        return centerpunchdata;
    }

    public void setCenterpunchdata(List<PunchData> centerpunchdata) {
        this.centerpunchdata = centerpunchdata;
    }
}
