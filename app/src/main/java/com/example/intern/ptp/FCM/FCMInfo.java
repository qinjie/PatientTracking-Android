package com.example.intern.ptp.FCM;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FCMInfo {
    @SerializedName("mac_address")
    @Expose
    private String macAddress;
    @SerializedName("fcm_token")
    @Expose
    private String fcmToken;

    public FCMInfo(String macAddress, String fcmToken) {
        this.macAddress = macAddress;
        this.fcmToken = fcmToken;
    }

    /**
     *
     * @return
     * The macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *
     * @param macAddress
     * The MAC
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     *
     * @return
     * The fcmToken
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     *
     * @param fcmToken
     * The fcm_token
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
