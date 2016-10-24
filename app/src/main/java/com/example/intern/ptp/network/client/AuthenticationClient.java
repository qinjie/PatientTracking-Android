package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServerCallback;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.models.LoginInfo;
import com.example.intern.ptp.network.models.LoginResult;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class AuthenticationClient {

    private static AuthenticationClient instance;

    private Bus bus;

    public static AuthenticationClient createClient(Bus bus) {
        if (instance == null) {
            instance = new AuthenticationClient(bus);
        }

        return instance;
    }

    public static AuthenticationClient getClient() {
        return instance;
    }

    private AuthenticationClient(Bus bus) {
        this.bus = bus;
    }


    public void checkToken(final Context context) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        Call<ResponseBody> call = api.getCheck();

        call.enqueue(new ServerCallback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                bus.post(new ServerResponse(ServerResponse.POST_CHECK_TOKEN, response.headers().get("result").equalsIgnoreCase("isNotExpired")));
            }
        });

    }

    public void login(final Context context, LoginInfo loginInfo) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        Call<LoginResult> call = api.getLogin(loginInfo);
        call.enqueue(new ServerCallback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                bus.post(new ServerResponse(ServerResponse.POST_LOGIN, response.body()));
            }
        });
    }
}
