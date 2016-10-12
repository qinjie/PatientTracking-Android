package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServerCallback;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.network.models.SearchParam;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ResidentClient {

    private static ResidentClient instance;

    private Bus bus;

    public static ResidentClient createClient(Bus bus) {
        if (instance == null) {
            instance = new ResidentClient(bus);
        }

        return instance;
    }

    public static ResidentClient getClient() {
        return instance;
    }

    private ResidentClient(Bus bus) {
        this.bus = bus;
    }

    public void getResident(final Context context, final String id) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to get a resident's information
        Call<Resident> call = api.getResident(id);
        call.enqueue(new ServerCallback<Resident>() {
            @Override
            public void onResponse(Call<Resident> call, Response<Resident> response) {
                bus.post(new ServerResponse(ServerResponse.GET_RESIDENT, response.body()));
            }
        });
    }

    public void listResidents(final Context context, final SearchParam param) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to get list of residents detected by the system within location timeout from server
        Call<List<Resident>> call = api.getSearch(param);
        call.enqueue(new ServerCallback<List<Resident>>() {
            @Override
            public void onResponse(final Call<List<Resident>> call, Response<List<Resident>> response) {
                List<Resident> residents = response.body();
                bus.post(new ServerResponse(ServerResponse.GET_RESIDENT_LIST, residents));
            }
        });
    }
}
