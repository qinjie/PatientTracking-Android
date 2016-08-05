package com.example.intern.ptp.Nearest;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

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
            // get username from Shared Preferences
            final String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

            while (true) {
                try {
                    // create an API service and set session token to request header
                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                    // create request object to get information of the resident nearest to the user corresponding to the username
                    Call<Resident> call = api.getNearest(username);
                    call.enqueue(new Callback<Resident>() {
                        @Override
                        public void onResponse(Call<Resident> call, Response<Resident> response) {
                            try {
                                // create an intent corresponding to broadcast receiver in the NearestFragment with the same tag equals "Preferences.nearest_broadcastTag + username"
                                Intent intent = new Intent(Preferences.nearest_broadcastTag + username);

                                // create a bundle to be able to put object as an extra to the intent
                                Bundle bundle = new Bundle();

                                // put result header from response to the bundle as an extra string
                                bundle.putString(Preferences.nearest_resultTag, response.headers().get("result"));

                                // put the nearest resident object to the above created bundle as an parcelable extra because it is not primitive data type
                                bundle.putParcelable(Preferences.nearest_residentTag, response.body());

                                // put bundle to to intent as an extra
                                intent.putExtras(bundle);

                                // broadcast the intent to the broadcast service in the NearestFragment
                                NearestService.this.sendBroadcast(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Resident> call, Throwable t) {
                            t.printStackTrace();
                            // create an intent corresponding to broadcast service in the NearestFragment with the same tag equals "Preferences.nearest_broadcastTag + username"
                            Intent intent = new Intent(Preferences.nearest_broadcastTag + username);

                            // notify that the connection is failed by putting to the intent an extra string
                            intent.putExtra(Preferences.nearest_resultTag, "connection_failure");

                            // broadcast the intent to the broadcast service in the NearestFragment
                            NearestService.this.sendBroadcast(intent);
                        }
                    });
                    Thread.sleep(Preferences.nearest_request_period);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
