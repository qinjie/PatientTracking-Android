package com.example.intern.ptp.utils.bus.response;

public class ServerResponse extends BusResponse {

    public static final String SERVER_ERROR = "server_error";
    public static final String GET_ALERT_COUNT = "get_alert_count";
    public static final String GET_ALERTS = "get_alerts";
    public static final String GET_RESIDENT = "get_resident";
    public static final String POST_TAKE_CARE = "post_take_care";
    public static final String GET_FLOORS = "get_floors";

    public ServerResponse(String type, Object response) {
        super(type, response);
    }
}
