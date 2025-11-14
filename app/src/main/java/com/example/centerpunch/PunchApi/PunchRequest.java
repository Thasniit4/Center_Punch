package com.example.centerpunch.PunchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PunchRequest {
    @SerializedName("emp_code")
    @Expose
    private String empCode;
    @SerializedName("p_flag")
    @Expose
    private String pFlag;

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getpFlag() {
        return pFlag;
    }

    public void setpFlag(String pFlag) {
        this.pFlag = pFlag;
    }

}
