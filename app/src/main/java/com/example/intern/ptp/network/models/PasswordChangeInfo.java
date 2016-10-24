package com.example.intern.ptp.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PasswordChangeInfo {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("current_password")
    @Expose
    private String currentPassword;
    @SerializedName("new_password")
    @Expose
    private String newPassword;
    @SerializedName("mac_address")
    @Expose
    private String macAddress;

    public PasswordChangeInfo(String username, String currentPassword, String newPassword, String macAddress) {
        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
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
     * @return The currentPassword
     */
    public String getCurrentPassword() {
        return currentPassword;
    }

    /**
     * @param currentPassword The current_password
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    /**
     * @return The newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword The new_password
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * @return The macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * @param macAddress The mac_address
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
