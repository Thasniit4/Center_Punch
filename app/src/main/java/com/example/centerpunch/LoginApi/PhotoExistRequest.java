package com.example.centerpunch.LoginApi;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoExistRequest {

    @SerializedName("p_emp_code")
    @Expose
    private String pEmpCode;

    public String getpEmpCode() {
        return pEmpCode;
    }

    public void setpEmpCode(String pEmpCode) {
        this.pEmpCode = pEmpCode;
    }

}