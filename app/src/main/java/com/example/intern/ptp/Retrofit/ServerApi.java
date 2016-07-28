package com.example.intern.ptp.Retrofit;

import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.FCM.FCMInfo;
import com.example.intern.ptp.Location.Location;
import com.example.intern.ptp.Login.LoginInfo;
import com.example.intern.ptp.Login.LoginResult;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Resident.SearchParam;
import com.example.intern.ptp.Setting.ChangeInfo;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    @POST("user/check")
    Call<ResponseBody> getCheck();

    @POST("user/login")
    Call<LoginResult> getLogin(@Body LoginInfo param);

    @POST("user/change")
    Call<String> setPassword(@Body ChangeInfo param);

    @POST("user/email")
    Call<String> getEmail(@Query("username") String username);

    @POST("user/receive")
    Call<String> setFCMToken(@Body FCMInfo param);

    @POST("user/search")
    Call<List<Resident>> getSearch(@Body SearchParam param);

    @POST("user/resident")
    Call<Resident> getResident(@Query("id") String id);

    @POST("user/nearest")
    Call<Resident> getNearest(@Query("username") String username);

    @POST("user/floor")
    Call<List<Location>> getFloor(@Query("id") String id);

    @POST("user/floors")
    Call<List<Location>> getFloors();

    @POST("user/alerts")
    Call<List<Alert>> getAlerts(@Query("id") String id, @Query("ok") String ok);

    @POST("user/takecare")
    Call<String> setTakecare(@Query("id") String id, @Query("username") String username);

    @POST("user/mappoints")
    Call<List<Resident>> getMappoints(@Query("floor_id") String id, @Query("username") String username);
}

