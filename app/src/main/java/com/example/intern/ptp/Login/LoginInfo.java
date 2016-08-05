package com.example.intern.ptp.Login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginInfo {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("mac_address")
    @Expose
    private String macAddress;

    public LoginInfo(String username, String password, String macAddress) {
        this.username = username;
        this.password = password;
        this.macAddress = macAddress;
    }

    /**
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * @param macAddress The MAC
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

}
