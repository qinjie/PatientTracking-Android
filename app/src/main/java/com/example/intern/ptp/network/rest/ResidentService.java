package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentService {

    private static ResidentService instance;

    private Bus bus;

    public static ResidentService createService(Bus bus) {
        if (instance == null) {
            instance = new ResidentService(bus);
        }

        return instance;
    }

    public static ResidentService getService() {
        return instance;
    }

    private ResidentService(Bus bus) {
        this.bus = bus;
    }

    public void getResident(final Context context, final String id) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get a resident's information
        Call<Resident> call = api.getResident(id);
        call.enqueue(new Callback<Resident>() {
            @Override
            public void onResponse(Call<Resident> call, Response<Resident> response) {
                try {
                    // if exception occurs or inconsistent database in server
                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                        return;
                    }

                    // if session is expired
                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                        Preferences.goLogin(context);
                        return;
                    }
                    bus.post(new ServerResponse(ServerResponse.GET_RESIDENT, response.body()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                t.printStackTrace();
                Preferences.showDialog(context, "Connection Failure", "Please check your network and try again!");

                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }

}
