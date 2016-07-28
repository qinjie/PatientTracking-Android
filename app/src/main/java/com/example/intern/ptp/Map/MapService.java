package com.example.intern.ptp.Map;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

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
            final String floorId = intent.getStringExtra(Preferences.floor_idTag);
            String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

            while (true) {
                try {
                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                    Call<List<Resident>> call = api.getMappoints(floorId, username);
                    call.enqueue(new Callback<List<Resident>>() {
                        @Override
                        public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                            try {
                                Intent intent = new Intent(Preferences.map_broadcastTag + floorId);
                                intent.putExtra(Preferences.map_resultTag, response.headers().get("result"));
                                ArrayList<Resident> res = (ArrayList<Resident>) response.body();
                                for (int i = 0; i < res.size(); i++) {
                                    if(!res.get(i).getId().equalsIgnoreCase("-1"))
                                        res.get(i).setColor(i % 2 == 0 ? ("" + Color.RED) : ("" + Color.BLUE));
                                }
                                intent.putParcelableArrayListExtra(Preferences.map_pointsTag, res);
                                MapService.this.sendBroadcast(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Resident>> call, Throwable t) {
                            t.printStackTrace();
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
