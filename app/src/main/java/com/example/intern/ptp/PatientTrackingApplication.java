package com.example.intern.ptp;

import android.app.Application;

import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.network.rest.AlertService;
import com.example.intern.ptp.network.rest.ResidentService;
import com.example.intern.ptp.utils.BusManager;
import com.squareup.otto.Bus;

public class PatientTrackingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusManager.getBus();

        AlertService.createService(this, bus);
        ResidentService.createService(this, bus);
    }

}
