package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServerCallback;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AlertClient {

    private static AlertClient instance;

    private Bus bus;

    public static AlertClient createClient(Bus bus) {
        if (instance == null) {
            instance = new AlertClient(bus);
        }

        return instance;
    }

    public static AlertClient getClient() {
        return instance;
    }

    private AlertClient(Bus bus) {
        this.bus = bus;
    }

    public void getAlerts(final Context context, final boolean onlyOngoing) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to get notification information
        Call<List<Alert>> call = api.getAlerts("all", onlyOngoing ? "0" : "all");
        call.enqueue(new ServerCallback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                bus.post(new ServerResponse(ServerResponse.GET_ALERTS, response.body()));

            }
        });
    }

    public void getAlertCount(final Context context) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);
        // create request object to check session time out
        Call<Integer> call = api.getAlertCount();

        call.enqueue(new ServerCallback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                bus.post(new ServerResponse(ServerResponse.GET_ALERT_COUNT, response.body()));
            }
        });
    }

    public void postTakeCare(final Context context, final Alert alert, final String username) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);
        Call<String> call = api.setTakecare(alert.getId(), username);

        call.enqueue(new ServerCallback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String res = response.body();

                if (res.equalsIgnoreCase("success") || !res.equalsIgnoreCase("failed")) {
                    alert.setOk("1");
                }

                bus.post(new ServerResponse(ServerResponse.POST_TAKE_CARE, alert));
            }
        });
    }
}
