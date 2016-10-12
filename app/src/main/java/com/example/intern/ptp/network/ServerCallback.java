package com.example.intern.ptp.network;

import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;

import retrofit2.Call;
import retrofit2.Callback;

public abstract class ServerCallback<T> implements Callback<T> {

    private Call<T> call;

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        this.call = call;

        Bus bus = BusManager.getBus();
        bus.post(new ServerError<T>(ServerError.ERROR_UNKNOWN, this));
    }

    public Call<T> getCall() {
        return call;
    }
}
