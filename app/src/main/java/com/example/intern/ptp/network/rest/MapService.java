package com.example.intern.ptp.network.rest;

import android.content.Context;

import com.example.intern.ptp.Map.Location;
import com.example.intern.ptp.Map.MapPointsResult;
import com.example.intern.ptp.Nearest.NearestResidentResult;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapService {

    private static MapService instance;

    private Bus bus;

    public static MapService createService(Bus bus) {
        if (instance == null) {
            instance = new MapService(bus);
        }

        return instance;
    }

    public static MapService getService() {
        return instance;
    }

    private MapService(Bus bus) {
        this.bus = bus;
    }

    public void getFloors(final Context context) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get a resident's information
        Call<List<Location>> call = api.getFloors();
        call.enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
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
                    bus.post(new ServerResponse(ServerResponse.GET_FLOOR_LIST, response.body()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                t.printStackTrace();
                Preferences.showDialog(context, "Connection Failure", "Please check your network and try again!");

                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, t));
            }
        });
    }

    public void getMapPoints(final Context context, final String floorId, final String username) {
        try {
            // create an API service and set session token to request header
            ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get pixel positions of all residents and the user corresponding to the above username if they are in the floor which has id equals the above floor id
            Call<List<Resident>> call = api.getMappoints(floorId, username);
            call.enqueue(new Callback<List<Resident>>() {
                @Override
                public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                    MapPointsResult result = new MapPointsResult();
                    result.setFloorId(floorId);
                    result.setResult(response.headers().get("result"));
                    result.setResidents(response.body());

                    bus.post(new ServerResponse(ServerResponse.GET_MAP_POINTS, result));
                }

                @Override
                public void onFailure(Call<List<Resident>> call, Throwable t) {
                    MapPointsResult result = new MapPointsResult();
                    result.setFloorId(floorId);
                    result.setResult("connection_failure");

                    bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, result));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getNearest(final Context context, final String username) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get information of the resident nearest to the user corresponding to the username
        Call<Resident> call = api.getNearest(username);
        call.enqueue(new Callback<Resident>() {
            @Override
            public void onResponse(Call<Resident> call, Response<Resident> response) {
                NearestResidentResult nearestResident = new NearestResidentResult();
                nearestResident.setUsername(username);
                nearestResident.setResult(response.headers().get("result"));
                nearestResident.setResident(response.body());

                bus.post(new ServerResponse(ServerResponse.GET_NEAREST_RESIDENT, nearestResident));
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                NearestResidentResult nearestResident = new NearestResidentResult();
                nearestResident.setUsername(username);
                nearestResident.setResult("connection_failure");

                bus.post(new ServerResponse(ServerResponse.SERVER_ERROR, nearestResident));
            }
        });
    }
}
