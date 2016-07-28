package com.example.intern.ptp.Resident;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Resident implements Parcelable{
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("firstname")
    @Expose
    private String firstname;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("nric")
    @Expose
    private String nric;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("contact")
    @Expose
    private String contact;
    @SerializedName("remark")
    @Expose
    private String remark;
    @SerializedName("lastmodified")
    @Expose
    private String lastmodified;
    @SerializedName("floor_id")
    @Expose
    private String floorId;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("coorx")
    @Expose
    private String coorx;
    @SerializedName("coory")
    @Expose
    private String coory;
    @SerializedName("pixelx")
    @Expose
    private String pixelx;
    @SerializedName("pixely")
    @Expose
    private String pixely;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("distance")
    @Expose
    private String distance;
    @SerializedName("nextofkin")
    @Expose
    private List<NextOfKin> nextofkin;


    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     *
     * @param firstname
     * The firstname
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     *
     * @return
     * The lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     *
     * @param lastname
     * The lastname
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    /**
     *
     * @return
     * The nric
     */
    public String getNric() {
        return nric;
    }

    /**
     *
     * @param nric
     * The nric
     */
    public void setNric(String nric) {
        this.nric = nric;
    }

    /**
     *
     * @return
     * The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     *
     * @param gender
     * The gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     *
     * @return
     * The birthday
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     *
     * @param birthday
     * The birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     *
     * @return
     * The contact
     */
    public String getContact() {
        return contact;
    }

    /**
     *
     * @param contact
     * The contact
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     *
     * @return
     * The remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     *
     * @param remark
     * The remark
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     *
     * @return
     * The lastmodified
     */
    public String getLastmodified() {
        return lastmodified;
    }

    /**
     *
     * @param lastmodified
     * The lastmodified
     */
    public void setLastmodified(String lastmodified) {
        this.lastmodified = lastmodified;
    }

    /**
     *
     * @return
     * The floorId
     */
    public String getFloorId() {
        return floorId;
    }

    /**
     *
     * @param floorId
     * The floor_id
     */
    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    /**
     *
     * @return
     * The label
     */
    public String getLabel() {
        return label;
    }

    /**
     *
     * @param label
     * The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     *
     * @return
     * The coorx
     */
    public String getCoorx() {
        return coorx;
    }

    /**
     *
     * @param coorx
     * The coorx
     */
    public void setCoorx(String coorx) {
        this.coorx = coorx;
    }

    /**
     *
     * @return
     * The coory
     */
    public String getCoory() {
        return coory;
    }

    /**
     *
     * @param coory
     * The coory
     */
    public void setCoory(String coory) {
        this.coory = coory;
    }

    /**
     *
     * @return
     * The pixelx
     */
    public String getPixelx() {
        return pixelx;
    }

    /**
     *
     * @param pixelx
     * The pixelx
     */
    public void setPixelx(String pixelx) {
        this.pixelx = pixelx;
    }

    /**
     *
     * @return
     * The pixely
     */
    public String getPixely() {
        return pixely;
    }

    /**
     *
     * @param pixely
     * The pixely
     */
    public void setPixely(String pixely) {
        this.pixely = pixely;
    }

    /**
     *
     * @return
     * The color
     */
    public String getColor() {
        return color;
    }

    /**
     *
     * @param color
     * The color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     *
     * @return
     * The distance
     */
    public String getDistance() {
        return distance;
    }

    /**
     *
     * @param distance
     * The distance
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }

    /**
     *
     * @return
     * The nextofkin
     */
    public List<NextOfKin> getNextofkin() {
        return nextofkin;
    }

    /**
     *
     * @param nextofkin
     * The nextofkin
     */
    public void setNextofkin(List<NextOfKin> nextofkin) {
        this.nextofkin = nextofkin;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(nric);
        dest.writeString(gender);
        dest.writeString(birthday);
        dest.writeString(contact);
        dest.writeString(remark);
        dest.writeString(lastmodified);
        dest.writeString(floorId);
        dest.writeString(label);
        dest.writeString(coorx);
        dest.writeString(coory);
        dest.writeString(pixelx);
        dest.writeString(pixely);
        dest.writeString(color);
        dest.writeString(distance);
    }
    protected Resident(Parcel in) {
        id = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        nric = in.readString();
        gender = in.readString();
        birthday = in.readString();
        contact = in.readString();
        remark = in.readString();
        lastmodified = in.readString();
        floorId = in.readString();
        label = in.readString();
        coorx = in.readString();
        coory = in.readString();
        pixelx = in.readString();
        pixely = in.readString();
        color = in.readString();
        distance = in.readString();
    }

    public static final Creator<Resident> CREATOR = new Creator<Resident>() {
        @Override
        public Resident createFromParcel(Parcel in) {
            return new Resident(in);
        }

        @Override
        public Resident[] newArray(int size) {
            return new Resident[size];
        }
    };
}
