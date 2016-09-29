package com.example.intern.ptp.utils;

import com.squareup.otto.Bus;

public class BusManager {

    private Bus bus;
    private static BusManager manager;

    private BusManager() {
        bus = new Bus();
    }

    public static Bus getBus() {
        if (manager == null) {
            manager = new BusManager();
        }

        return manager.bus;
    }
}
