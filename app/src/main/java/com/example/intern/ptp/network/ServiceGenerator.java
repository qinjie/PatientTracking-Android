package com.example.intern.ptp.network;

import android.content.Context;

import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.UserManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.squareup.otto.Bus;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {


    private static final String API_BASE_URL = Preferences.root;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    // build base URL for the API service and add converter factory for serialization and deserialization of objects
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());


    /**
     * create an API service related to an API interface
     */
    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    /**
     * create an API service related to an API interface and set "token" header for requests
     */
    public static <S> S createService(Class<S> serviceClass, Context context) {
        final String token = UserManager.getSessionToken(context);

        if (token != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("token", token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                Bus bus = BusManager.getBus();
                String result = response.headers().get("result");

                if (result != null) {
                    if (result.equalsIgnoreCase("failed")) {
                        throw new IOException();
                    } else if (!result.equalsIgnoreCase("isNotExpired")) {
                        bus.post(new ServerError<>(ServerError.ERROR_TOKEN_EXPIRED, null));
                    }
                }

                return response;
            }
        });


        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}