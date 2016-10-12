package com.example.intern.ptp.network;

import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Callback;

public abstract class ServerCallback<T> implements Callback<T> {

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Bus bus = BusManager.getBus();
        bus.post(new ServerResponse(ServerResponse.ERROR_UNKNOWN, t));
    }
}
