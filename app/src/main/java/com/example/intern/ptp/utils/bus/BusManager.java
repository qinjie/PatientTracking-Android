package com.example.intern.ptp.utils.bus;

import com.squareup.otto.Bus;

public class BusManager {

    private Bus bus;
    private static BusManager manager;

    private BusManager() {
        bus = new MainThreadBus();
    }

    public static Bus getBus() {
        if (manager == null) {
            manager = new BusManager();
        }

        return manager.bus;
    }

}
