package com.example.intern.ptp.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NextOfKin implements Parcelable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("nric")
    @Expose
    private String nric;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("contact")
    @Expose
    private String contact;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("relation")
    @Expose
    private String relation;

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
     * @return The nric
     */
    public String getNric() {
        return nric;
    }

    /**
     * @param nric The nric
     */
    public void setNric(String nric) {
        this.nric = nric;
    }

    /**
     * @return The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The contact
     */
    public String getContact() {
        return contact;
    }

    /**
     * @param contact The contact
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark The remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return The relation
     */
    public String getRelation() {
        return relation;
    }

    /**
     * @param relation The relation
     */
    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nric);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(contact);
        dest.writeString(email);
        dest.writeString(remark);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeString(relation);
    }

    protected NextOfKin(Parcel in) {
        id = in.readString();
        nric = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        contact = in.readString();
        email = in.readString();
        remark = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        relation = in.readString();
    }

    public static final Creator<NextOfKin> CREATOR = new Creator<NextOfKin>() {
        @Override
        public NextOfKin createFromParcel(Parcel in) {
            return new NextOfKin(in);
        }

        @Override
        public NextOfKin[] newArray(int size) {
            return new NextOfKin[size];
        }
    };
}
