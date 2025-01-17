package com.example.intern.ptp.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.example.intern.ptp.network.client.MapClient;
import com.example.intern.ptp.network.models.NearestResidentResult;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class NearestService extends IntentService {

    public NearestService() {
        super("NearestService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusManager.getBus();
        bus.register(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

        MapClient service = MapClient.getClient();

        while (true) {
            try {
                service.getNearest(getApplicationContext(), username);

                Thread.sleep(Preferences.nearest_request_period);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_NEAREST_RESIDENT)) {
            broadcastNearestResident((NearestResidentResult) event.getMessage());
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {
            if (serverError.getMessage() instanceof NearestResidentResult) {
                broadcastError((NearestResidentResult) serverError.getMessage());
            }
        }
    }

    private void broadcastNearestResident(NearestResidentResult result) {
        Intent intent = new Intent(Preferences.nearest_broadcastTag + result.getUsername());

        Bundle bundle = new Bundle();
        bundle.putString(Preferences.nearest_resultTag, result.getResult());
        bundle.putParcelable(Preferences.nearest_residentTag, result.getResident());
        intent.putExtras(bundle);

        NearestService.this.sendBroadcast(intent);
    }

    private void broadcastError(NearestResidentResult result) {
        Intent intent = new Intent(Preferences.nearest_broadcastTag + result.getUsername());
        intent.putExtra(Preferences.nearest_resultTag, result.getResult());

        NearestService.this.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }
}
