package com.example.intern.ptp.utils.bus.response;

public class ServerResponse extends BusMessage {

    public static final String GET_ALERT_COUNT = "get_alert_count";
    public static final String GET_ALERTS = "get_alerts";
    public static final String GET_RESIDENT = "get_resident";
    public static final String POST_TAKE_CARE = "post_take_care";
    public static final String GET_FLOOR_LIST = "get_floors";
    public static final String POST_LOGIN = "post_login";
    public static final String POST_CHECK_TOKEN = "post_check_token";
    public static final String GET_MAP_POINTS = "get_map_points";
    public static final String GET_NEAREST_RESIDENT = "get_nearest_resident";
    public static final String GET_RESIDENT_LIST = "get_resident_list";
    public static final String POST_CHANGE_PASSWORD = "post_change_password";


    public ServerResponse(String type, Object message) {
        super(type, message);
    }
}
