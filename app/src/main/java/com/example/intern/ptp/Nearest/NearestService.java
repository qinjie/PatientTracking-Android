package com.example.intern.ptp.Nearest;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearestService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    private ServerApi api;

    public NearestService() {
        super("NearestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

            while (true) {
                try {
                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                    Call<Resident> call = api.getNearest(username);
                    call.enqueue(new Callback<Resident>() {
                        @Override
                        public void onResponse(Call<Resident> call, Response<Resident> response) {
                            try {
                                Intent intent = new Intent(Preferences.nearest_broadcastTag + username);
                                Bundle bundle = new Bundle();
                                bundle.putString(Preferences.nearest_resultTag, response.headers().get("result"));
                                bundle.putParcelable(Preferences.nearest_residentTag, response.body());
                                intent.putExtras(bundle);
                                NearestService.this.sendBroadcast(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Resident> call, Throwable t) {
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
