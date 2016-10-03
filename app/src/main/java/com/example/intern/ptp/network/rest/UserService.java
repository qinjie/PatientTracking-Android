package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Setting.PasswordChangeInfo;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserService {

    private static UserService instance;

    private Bus bus;

    public static UserService createService(Bus bus) {
        if (instance == null) {
            instance = new UserService(bus);
        }

        return instance;
    }

    public static UserService getService() {
        return instance;
    }

    private UserService(Bus bus) {
        this.bus = bus;
    }

    public void changePassword(final Context context, PasswordChangeInfo changeInfo) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to requesting change passowrd
        Call<String> call = api.setPassword(changeInfo);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                // get response information from server
                String result = response.body();

                // if exception occurs or inconsistent database in server
                if (response.headers().get("result").equalsIgnoreCase("failed") || result.equalsIgnoreCase("failed")) {
                    return;
                }

                // if session is expired
                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                    Preferences.goLogin(context);
                    return;
                }

                bus.post(new ServerResponse(ServerResponse.POST_CHANGE_PASSWORD, result));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }
}
