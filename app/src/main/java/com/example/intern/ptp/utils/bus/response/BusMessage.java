package com.example.intern.ptp.utils.bus.response;

public abstract class BusMessage {
    private String type;
    private Object message;

    public BusMessage(String type, Object message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public Object getMessage() {
        return message;
    }
}
