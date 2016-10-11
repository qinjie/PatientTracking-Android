package com.example.intern.ptp.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Alert implements Parcelable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("resident_id")
    @Expose
    private String residentId;
    @SerializedName("firstname")
    @Expose
    private String firstname;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("last_position")
    @Expose
    private String lastPosition;
    @SerializedName("last_position_label")
    @Expose
    private String lastPositionLabel;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("ok")
    @Expose
    private String ok;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("type")
    @Expose
    private String type;



    public Alert(String id, String residentId, String firstname, String lastname, String lastPosition, String userId, String username, String ok) {
        this.id = id;
        this.residentId = residentId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.lastPosition = lastPosition;
        this.userId = userId;
        this.username = username;
        this.ok = ok;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The residentId
     */
    public String getResidentId() {
        return residentId;
    }

    /**
     * @param residentId The resident_id
     */
    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }

    /**
     * @return The firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @param firstname The firstname
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return The lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname The lastname
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     * @return The lastPosition
     */
    public String getLastPosition() {
        return lastPosition;
    }

    /**
     * @param lastPosition The last_position
     */
    public void setLastPosition(String lastPosition) {
        this.lastPosition = lastPosition;
    }

    public String getLastPositionLabel() {
        return lastPositionLabel;
    }

    public void setLastPositionLabel(String lastPositionLabel) {
        this.lastPositionLabel = lastPositionLabel;
    }

    /**
     * @return The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
     * @return The ok
     */
    public String getOk() {
        return ok;
    }

    /**
     * @param ok The ok
     */
    public void setOk(String ok) {
        this.ok = ok;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOngoing() {
        return getOk().equalsIgnoreCase("0");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(residentId);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(lastPosition);
        dest.writeString(lastPositionLabel);
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(ok);
        dest.writeString(createdAt);
        dest.writeString(type);
    }

    protected Alert(Parcel in) {
        id = in.readString();
        residentId = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        lastPosition = in.readString();
        lastPositionLabel = in.readString();
        userId = in.readString();
        username = in.readString();
        ok = in.readString();
        createdAt = in.readString();
        type = in.readString();
    }

    public static final Creator<Alert> CREATOR = new Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };
}
