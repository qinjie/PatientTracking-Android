package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.network.models.SearchParam;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

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
                bus.post(new ServerResponse(ServerResponse.GET_RESIDENT, response.body()));
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.ERROR_UNKNOWN, t));
            }
        });
    }

    public void listResidents(final Context context, final SearchParam param) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get list of residents detected by the system within location timeout from server
        Call<List<Resident>> call = api.getSearch(param);
        call.enqueue(new Callback<List<Resident>>() {
            @Override
            public void onResponse(final Call<List<Resident>> call, Response<List<Resident>> response) {
                List<Resident> residents = response.body();
                bus.post(new ServerResponse(ServerResponse.GET_RESIDENT_LIST, residents));
            }

            @Override
            public void onFailure(Call<List<Resident>> call, Throwable t) {
                bus.post(new ServerResponse(ServerResponse.ERROR_UNKNOWN, t));
            }
        });
    }
}
