package com.example.intern.ptp.utils.bus.response;

public class NotificationResponse extends BusResponse {

    public final static String MESSAGE_RECEIVED = "notification_message_received";

    public NotificationResponse(String type, Object response) {
        super(type, response);
    }
}
