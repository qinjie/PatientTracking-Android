package com.example.intern.ptp;

import android.app.Application;

import com.example.intern.ptp.network.rest.AlertService;
import com.example.intern.ptp.network.rest.AuthenticationService;
import com.example.intern.ptp.network.rest.MapService;
import com.example.intern.ptp.network.rest.ResidentService;
import com.example.intern.ptp.network.rest.UserService;
import com.example.intern.ptp.utils.bus.BusManager;
import com.squareup.otto.Bus;

public class PatientTrackingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusManager.getBus();

        AlertService.createService(bus);
        AuthenticationService.createService(bus);
        MapService.createService(bus);
        ResidentService.createService(bus);
        UserService.createService(bus);
    }
}
