package com.example.intern.ptp.Map;

import android.app.IntentService;
import android.content.Intent;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    private ServerApi api;

    public MapService() {
        super("MapService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // get floor id in the intent received from MapFragment
            final String floorId = intent.getStringExtra(Preferences.floor_idTag);

            // get username from Shared Preferences
            String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

            while (true) {
                try {
                    // create an API service and set session token to request header
                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                    // create request object to get pixel positions of all residents and the user corresponding to the above username if they are in the floor which has id equals the above floor id
                    Call<List<Resident>> call = api.getMappoints(floorId, username);
                    call.enqueue(new Callback<List<Resident>>() {
                        @Override
                        public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                            try {
                                // create an intent corresponding to broadcast receiver in the MapFragment with the same tag equals "Preferences.map_broadcastTag + floorId"
                                Intent intent = new Intent(Preferences.map_broadcastTag + floorId);

                                // put result header from response to the intent as an extra string
                                intent.putExtra(Preferences.map_resultTag, response.headers().get("result"));

                                // get list of all residents' pixel positions in the floor
                                // and the user's positions as the last element if it is also detected the floor, in this case the last element's id equals "-1"
                                ArrayList<Resident> res = (ArrayList<Resident>) response.body();

                                // put list of map points to the above created intent as an parcelable extra because res is a list of object, not a primitive data type
                                intent.putParcelableArrayListExtra(Preferences.map_pointsTag, res);

                                // broadcast the intent to the broadcast service in the MapFragment
                                MapService.this.sendBroadcast(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Resident>> call, Throwable t) {
                            t.printStackTrace();

                            // create an intent corresponding to broadcast service in the MapFragment with the same tag equals "Preferences.map_broadcastTag + floorId"
                            Intent intent = new Intent(Preferences.map_broadcastTag + floorId);

                            // notify that the connection is failed by putting to the intent an extra string
                            intent.putExtra(Preferences.map_resultTag, "connection_failure");

                            // broadcast the intent to the broadcast service in the MapFragment
                            MapService.this.sendBroadcast(intent);
                        }
                    });
                    Thread.sleep(Preferences.map_request_period);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
