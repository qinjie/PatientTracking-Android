package com.example.intern.ptp.network;

import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.network.models.FCMInfo;
import com.example.intern.ptp.network.models.LoginInfo;
import com.example.intern.ptp.network.models.LoginResult;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.network.models.SearchParam;
import com.example.intern.ptp.network.models.PasswordChangeInfo;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ServerApi {

    // check session timeout
    @POST("user/check")
    Call<ResponseBody> getCheck();

    // login
    @POST("user/login")
    Call<LoginResult> getLogin(@Body LoginInfo param);

    // change password
    @POST("user/change")
    Call<String> setPassword(@Body PasswordChangeInfo param);

    // get email of a user
    @POST("user/email")
    Call<String> getEmail(@Query("username") String username);

    // send a Firebase Cloud Messaging (FCM) token to server
    @POST("user/receive")
    Call<String> setFCMToken(@Body FCMInfo param);

    // search resident present in floors within location timeout defined in server
    @POST("user/search")
    Call<List<Resident>> getSearch(@Body SearchParam param);

    // get detail information of a resident
    @POST("user/resident")
    Call<Resident> getResident(@Query("id") String id);

    // get basic information of the nearest resident to a user
    @POST("user/nearest")
    Call<Resident> getNearest(@Query("username") String username);

    // get detail information of one or all floors (id = "all")
    @POST("user/floor")
    Call<List<Location>> getFloor(@Query("id") String id);

    // get basic information of all floors
    @POST("user/floors")
    Call<List<Location>> getFloors();

    // get all alerts basing on id and an ok param - takencare or untakencare status
    // in order to get information of a specific notification use ok = 'all'
    // also get user_id and username who has taken care of a notification
    @POST("user/alerts")
    Call<List<Alert>> getAlerts(@Query("id") String id, @Query("ok") String ok);

    @POST("user/alertcount")
    Call<Integer> getAlertCount();

    // get notified by untakencare notifications
    @POST("user/alertuntakencare")
    Call<String> notifyUntakenCareAlerts(@Query("mac_address") String macAddress);

    // user takes care of an alert
    @POST("user/takecare")
    Call<String> setTakecare(@Query("id") String id, @Query("username") String username);

    // get pixels coordinates of all residents and a specific user in a specific floor map
    // the coordinates of the user corresponding to the username is added as the last object with "id" equals "-1" only if the user is present in the floor within location timeout defined in server
    @POST("user/mappoints")
    Call<List<Resident>> getMappoints(@Query("floor_id") String id, @Query("username") String username);
}