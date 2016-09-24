package com.example.intern.ptp.network.rest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.squareup.otto.Bus;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertService {

    private static AlertService instance;

    private Context context;
    private Bus bus;

    public static AlertService createService(Context context, Bus bus) {
        if (instance == null) {
            instance = new AlertService(context, bus);
        }

        return instance;
    }

    public static AlertService getService() {
        return instance;
    }

    private AlertService(Context context, Bus bus) {
        this.context = context;
        this.bus = bus;
    }

    public void getAlertCount() {
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

    public void postTakeCare(String id, String username) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to send a take-care-action request to server
        Call<String> call = api.setTakecare(id, username);
        Preferences.showLoading(context);
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
                Preferences.dismissLoading();

                bus.post(new ServerResponse(ServerResponse.POST_TAKE_CARE, response.body()));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();
                Preferences.showDialog(context, "Connection Failure", "Please check your network and try again!");

                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }
}
