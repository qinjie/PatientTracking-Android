package com.example.intern.ptp.network.client;

import android.content.Context;

import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServerCallback;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.network.models.MapPointsResult;
import com.example.intern.ptp.network.models.NearestResidentResult;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MapClient {

    private static MapClient instance;

    private Bus bus;

    public static MapClient createClient(Bus bus) {
        if (instance == null) {
            instance = new MapClient(bus);
        }

        return instance;
    }

    public static MapClient getClient() {
        return instance;
    }

    private MapClient(Bus bus) {
        this.bus = bus;
    }

    public void getFloors(final Context context) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to get a resident's information
        Call<List<Location>> call = api.getFloors();
        call.enqueue(new ServerCallback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                bus.post(new ServerResponse(ServerResponse.GET_FLOOR_LIST, response.body()));
            }
        });
    }

    public void getMapPoints(final Context context, final String floorId, final String username) {
        try {
            // create an API service and set session token to request header
            ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

            // create request object to get pixel positions of all residents and the user corresponding to the above username if they are in the floor which has id equals the above floor id
            Call<List<Resident>> call = api.getMappoints(floorId, username);
            call.enqueue(new ServerCallback<List<Resident>>() {
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

                    bus.post(new ServerError<>(ServerError.ERROR_UNKNOWN, this, result));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getNearest(final Context context, final String username) {
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, context);

        // create request object to get information of the resident nearest to the user corresponding to the username
        Call<Resident> call = api.getNearest(username);
        call.enqueue(new ServerCallback<Resident>() {
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

                bus.post(new ServerError<>(ServerError.ERROR_UNKNOWN, this, nearestResident));
            }
        });
    }
}
