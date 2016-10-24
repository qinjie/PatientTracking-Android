package com.example.intern.ptp.utils.bus.response;

import com.example.intern.ptp.network.ServerCallback;

import retrofit2.Call;
import retrofit2.Callback;

public class ServerError<T> extends BusMessage {

    public static final String ERROR_TOKEN_EXPIRED = "error_token_expired";
    public static final String ERROR_UNKNOWN = "unknown_error";

    private ServerCallback<T> callback;

    public ServerError(String type, ServerCallback<T> callback) {
        super(type, null);

        this.callback = callback;
    }

    public ServerError(String type, ServerCallback<T> callback, Object message) {
        super(type, message);

        this.callback = callback;
    }

    public void retry() {
        if (callback != null) {
            Call<T> call = callback.getCall();
            call.clone().enqueue(callback);
        }
    }
}
