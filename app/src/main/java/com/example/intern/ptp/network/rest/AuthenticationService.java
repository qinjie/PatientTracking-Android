package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Login.LoginInfo;
import com.example.intern.ptp.Login.LoginResult;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationService {

    private static AuthenticationService instance;

    private Bus bus;

    public static AuthenticationService createService(Bus bus) {
        if (instance == null) {
            instance = new AuthenticationService(bus);
        }

        return instance;
    }

    public static AuthenticationService getService() {
        return instance;
    }

    private AuthenticationService(Bus bus) {
        this.bus = bus;
    }


    public void checkToken(final Context context) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        Call<ResponseBody> call = api.getCheck();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                bus.post(new ServerResponse(ServerResponse.POST_CHECK_TOKEN, response.headers().get("result").equalsIgnoreCase("isNotExpired")));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });

    }

    public void login(final Context context, LoginInfo loginInfo) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        Call<LoginResult> call = api.getLogin(loginInfo);
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                bus.post(new ServerResponse(ServerResponse.POST_LOGIN, response.body()));
            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }
}
