package com.example.intern.ptp.network.rest;

public class ServerResponse {
    private String type;
    private Object response;

    public static final String SERVER_ERROR = "server_error";
    public static final String GET_ALERT_COUNT = "get_alert_count";
    public static final String GET_RESIDENT = "get_resident";
    public static final String POST_TAKE_CARE = "post_take_care";

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
