package com.example.intern.ptp.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FCMInfo {
    @SerializedName("fcm_token")
    @Expose
    private String fcmToken;

    public FCMInfo(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * @return The fcmToken
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * @param fcmToken The fcm_token
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
