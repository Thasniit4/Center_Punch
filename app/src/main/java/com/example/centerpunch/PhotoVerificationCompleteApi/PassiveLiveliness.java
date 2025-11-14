package com.example.centerpunch.PhotoVerificationCompleteApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PassiveLiveliness {

    @SerializedName("liveness")
    @Expose
    private Boolean liveness;
    @SerializedName("score")
    @Expose
    private Integer score;

    public Boolean getLiveness() {
        return liveness;
    }

    public void setLiveness(Boolean liveness) {
        this.liveness = liveness;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

}
