package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertService {

    private static AlertService instance;

    private Bus bus;

    public static AlertService createService(Bus bus) {
        if (instance == null) {
            instance = new AlertService(bus);
        }

        return instance;
    }

    public static AlertService getService() {
        return instance;
    }

    private AlertService(Bus bus) {
        this.bus = bus;
    }

    public void getAlerts(final Context context, final boolean onlyOngoing) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get notification information
        Call<List<Alert>> call = api.getAlerts("all", onlyOngoing ? "0" : "all");
        call.enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {

                // if exception occurs or inconsistent database in server
                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                    return;
                }

                // if session is expired
                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                    Preferences.goLogin(context);
                    return;
                }

                bus.post(new ServerResponse(ServerResponse.GET_ALERTS, response.body()));

            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }

    public void getAlertCount(final Context context) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to check session time out
        Call<Integer> call = api.getAlertCount();

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                bus.post(new ServerResponse(ServerResponse.GET_ALERT_COUNT, response.body()));
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }

    public void postTakeCare(final Context context, String id, String username) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
        Call<String> call = api.setTakecare(id, username);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    // if exception occurs or inconsistent database in server
                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                        Preferences.dismissLoading();
                        Preferences.showDialog(context, "Server Error", "Please try again !");
                        return;
                    }

                    // if session is expired
                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                        Preferences.goLogin(context);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                bus.post(new ServerResponse(ServerResponse.POST_TAKE_CARE, response.body()));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();

                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }
}
