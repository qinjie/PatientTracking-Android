package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServerCallback;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.models.PasswordChangeInfo;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Response;

public class UserClient {

    private static UserClient instance;

    private Bus bus;

    public static UserClient createClient(Bus bus) {
        if (instance == null) {
            instance = new UserClient(bus);
        }

        return instance;
    }

    public static UserClient getClient() {
        return instance;
    }

    private UserClient(Bus bus) {
        this.bus = bus;
    }

    public void changePassword(final Context context, PasswordChangeInfo changeInfo) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to requesting change password
        Call<String> call = api.setPassword(changeInfo);
        call.enqueue(new ServerCallback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                bus.post(new ServerResponse(ServerResponse.POST_CHANGE_PASSWORD, response.body()));
            }
        });
    }
}
