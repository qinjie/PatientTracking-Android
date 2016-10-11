package com.example.intern.ptp.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.network.client.MapService;
import com.example.intern.ptp.network.models.MapPointsResult;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class MapPointsService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    public MapPointsService() {
        super("MapPointsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusManager.getBus();
        bus.register(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // get floor id in the intent received from MapFragment
            final String floorId = intent.getStringExtra(Preferences.floor_idTag);

            // get username from Shared Preferences
            String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

            MapService service = MapService.getService();

            while (true) {
                try {
                    service.getMapPoints(getApplicationContext(), floorId, username);

                    Thread.sleep(Preferences.map_request_period);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastMapPoints(MapPointsResult result) {
        Intent intent = new Intent(Preferences.map_broadcastTag + result.getFloorId());
        intent.putExtra(Preferences.map_resultTag, result.getResult());

        ArrayList<Resident> residents = new ArrayList<>(result.getResidents());
        intent.putParcelableArrayListExtra(Preferences.map_pointsTag, residents);

        MapPointsService.this.sendBroadcast(intent);
    }

    private void broadcastError(MapPointsResult result) {
        Intent intent = new Intent(Preferences.map_broadcastTag + result.getFloorId());
        intent.putExtra(Preferences.map_resultTag, result.getResult());

        MapPointsService.this.sendBroadcast(intent);
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_MAP_POINTS)) {
            broadcastMapPoints((MapPointsResult) event.getResponse());
        } else if (event.getType().equals(ServerResponse.ERROR_UNKNOWN)) {
            broadcastError((MapPointsResult) event.getResponse());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }
}
