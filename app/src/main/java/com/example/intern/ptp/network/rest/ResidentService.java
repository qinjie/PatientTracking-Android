package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentService {

    private static ResidentService instance;

    private Context context;
    private Bus bus;

    public static ResidentService createService(Context context, Bus bus) {
        if (instance == null) {
            instance = new ResidentService(context, bus);
        }

        return instance;
    }

    public static ResidentService getService() {
        return instance;
    }

    private ResidentService(Context context, Bus bus) {
        this.context = context;
        this.bus = bus;
    }

    public void getResident(String id) {
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
