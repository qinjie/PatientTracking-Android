package com.example.intern.ptp.Map;

import com.example.intern.ptp.Resident.Resident;

import java.util.List;

public class MapPointsResult {
    private String floorId;
    private String result;
    private List<Resident> residents;

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Resident> getResidents() {
        return residents;
    }

    public void setResidents(List<Resident> residents) {
        this.residents = residents;
    }
}
