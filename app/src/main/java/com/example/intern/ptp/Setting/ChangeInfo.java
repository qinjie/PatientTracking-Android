package com.example.intern.ptp.Setting;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChangeInfo {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("current_password")
    @Expose
    private String currentPassword;
    @SerializedName("new_password")
    @Expose
    private String newPassword;

    public ChangeInfo(String username, String currentPassword, String newPassword) {
        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    /**

     *
     * @return
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The currentPassword
     */
    public String getCurrentPassword() {
        return currentPassword;
    }

    /**
     *
     * @param currentPassword
     * The current_password
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    /**
     *
     * @return
     * The newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     *
     * @param newPassword
     * The new_password
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
