package com.example.intern.ptp.utils.bus.response;

public class NotificationMessage extends BusMessage {

    public final static String MESSAGE_RECEIVED = "notification_message_received";

    public NotificationMessage(String type, Object message) {
        super(type, message);
    }
}
