package com.example.intern.ptp.utils.bus.response;

public abstract class BusResponse {
    private String type;
    private Object response;

    public BusResponse(String type, Object response) {
        this.type = type;
        this.response = response;
    }

    public String getType() {
        return type;
    }

    public Object getResponse() {
        return response;
    }
}
