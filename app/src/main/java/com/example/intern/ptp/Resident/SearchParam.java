package com.example.intern.ptp.Resident;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchParam {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("sort")
    @Expose
    private String sort;
    @SerializedName("drec")
    @Expose
    private String drec;

    public SearchParam(String name, String location, String sort, String drec) {
        this.name = name;
        this.location = location;
        this.sort = sort;
        this.drec = drec;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return The sort
     */
    public String getSort() {
        return sort;
    }

    /**
     * @param sort The sort
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * @return The drec
     */
    public String getDrec() {
        return drec;
    }

    /**
     * @param drec The drec
     */
    public void setDrec(String drec) {
        this.drec = drec;
    }


}
