package com.example.centerpunch.PunchApi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PunchData {
    @SerializedName("emP_CODE")
    @Expose
    private Integer emPCODE;
    @SerializedName("emP_NAME")
    @Expose
    private String emPNAME;
    @SerializedName("alert")
    @Expose
    private String alert;
    @SerializedName("statuS_ID")
    @Expose
    private Integer statuSID;
    @SerializedName("brancH_ID")
    @Expose
    private Integer brancHID;
    @SerializedName("posT_ID")
    @Expose
    private Integer posTID;
    @SerializedName("centeR_ID")
    @Expose
    private String centeRID;
    @SerializedName("centeR_NAME")
    @Expose
    private String centeRNAME;
    @SerializedName("geO_LAT")
    @Expose
    private String geOLAT;
    @SerializedName("geO_LONG")
    @Expose
    private String geOLONG;
    @SerializedName("reqcount")
    @Expose
    private Integer reqcount;

    public Integer getEmPCODE() {
        return emPCODE;
    }

    public void setEmPCODE(Integer emPCODE) {
        this.emPCODE = emPCODE;
    }

    public String getEmPNAME() {
        return emPNAME;
    }

    public void setEmPNAME(String emPNAME) {
        this.emPNAME = emPNAME;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Integer getStatuSID() {
        return statuSID;
    }

    public void setStatuSID(Integer statuSID) {
        this.statuSID = statuSID;
    }

    public Integer getBrancHID() {
        return brancHID;
    }

    public void setBrancHID(Integer brancHID) {
        this.brancHID = brancHID;
    }

    public Integer getPosTID() {
        return posTID;
    }

    public void setPosTID(Integer posTID) {
        this.posTID = posTID;
    }

    public String getCenteRID() {
        return centeRID;
    }

    public void setCenteRID(String centeRID) {
        this.centeRID = centeRID;
    }

    public String getCenteRNAME() {
        return centeRNAME;
    }

    public void setCenteRNAME(String centeRNAME) {
        this.centeRNAME = centeRNAME;
    }

    public String getGeOLAT() {
        return geOLAT;
    }

    public void setGeOLAT(String geOLAT) {
        this.geOLAT = geOLAT;
    }

    public String getGeOLONG() {
        return geOLONG;
    }

    public void setGeOLONG(String geOLONG) {
        this.geOLONG = geOLONG;
    }

    public Integer getReqcount() {
        return reqcount;
    }

    public void setReqcount(Integer reqcount) {
        this.reqcount = reqcount;
    }
}
