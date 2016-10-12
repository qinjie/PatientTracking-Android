package com.example.intern.ptp;

import android.app.Application;
import android.widget.Toast;

import com.example.intern.ptp.network.client.AlertClient;
import com.example.intern.ptp.network.client.AuthenticationClient;
import com.example.intern.ptp.network.client.MapClient;
import com.example.intern.ptp.network.client.ResidentClient;
import com.example.intern.ptp.network.client.UserClient;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class PatientTrackingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusManager.getBus();
        bus.register(this);

        AlertClient.createClient(bus);
        AuthenticationClient.createClient(bus);
        MapClient.createClient(bus);
        ResidentClient.createClient(bus);
        UserClient.createClient(bus);
    }


    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.ERROR_TOKEN_EXPIRED)) {
            Preferences.goLogin(this);
        } else if (event.getType().equals(ServerResponse.ERROR_UNKNOWN)) {
            Toast.makeText(this, R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
        }
    }
}
