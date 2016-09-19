package com.example.intern.ptp.network.rest;

public class ServerResponse {

    private String type;
    private Object response;

    public static final String SERVER_ERROR = "server_error";
    public static final String GET_ALERT_COUNT = "alert_count";

    public ServerResponse(String type, Object response) {
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
