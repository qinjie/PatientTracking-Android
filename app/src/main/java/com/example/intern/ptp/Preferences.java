package com.example.intern.ptp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.intern.ptp.FCM.FCMInfo;
import com.example.intern.ptp.Login.LoginActivity;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Preferences {
    public static final String root = "http://172.18.202.19/advanced/api/web/"; //localhost
    public static final String imageRoot = "http://172.18.202.19/advanced/backend/web/";
//    public static final String root = "http://128.199.209.227/patient-tracking-web/api/web/"; //server
//    public static final String imageRoot = "http://128.199.209.227/patient-tracking-web/backend/web/";

    public static final String SharedPreferencesTag = "PTP_Pref";
    public static final String resident_idTag = "id";
    public static final String floor_idTag = "floor_id";
    public static final String floorFileParthTag = "floor_file_path";
    public static final String nearest_broadcastTag = "com.example.intern.ptp.intent.NEAREST";
    public static final String nearest_residentTag = "com.example.intern.ptp.intent.nearest_resident";
    public static final String nearest_resultTag = "com.example.intern.ptp.intent.nearest_result";
    public static final String map_broadcastTag = "com.example.intern.ptp.intent.MAP";
    public static final String map_pointsTag = "com.example.intern.ptp.intent.map_points";
    public static final String map_resultTag = "com.example.intern.ptp.intent.map_result";
    public static final String notify_Tag = "com.example.intern.ptp.intent.PTP_notification";
    public static final int map_request_period = 2000;
    public static final int SharedPreferences_ModeTag = Context.MODE_PRIVATE;

    public static ProgressDialog loading;
    public static boolean isShownLoading = false;

    public static int requestCode = 0;
    public static void goLogin(final Context context){
        try {
            String fcmToken = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("fcm_token", "");
            boolean fcmTokenStatus = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getBoolean("fcm_token_status", false);

            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().clear().apply();

            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putString("fcm_token", fcmToken).apply();
            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean("fcm_token_status", fcmTokenStatus).apply();

            if (isShownLoading) {
                isShownLoading = false;
                loading.dismiss();
            }

            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void showLoading(final Context context){
        try {
            if (!isShownLoading) {
                isShownLoading = true;
                loading = ProgressDialog.show(context, context.getString(R.string.loading_title), context.getString(R.string.loading_message));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void dismissLoading(){
        try {
            if (isShownLoading) {
                isShownLoading = false;
                loading.dismiss();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void kill(final Context context, String serviceName) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            String pricessName = context.getPackageName() + serviceName;
            for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {

                if (next.processName.equals(pricessName)) {
                    android.os.Process.killProcess(next.pid);
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void checkFcmTokenStatus(final Context context){
        try {
            boolean fcmTokenStatus = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getBoolean("fcm_token_status", false);
            String fcmToken = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("fcm_token", "");
            if (!fcmTokenStatus && !fcmToken.equalsIgnoreCase("")) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wInfo = wifiManager.getConnectionInfo();
                String macAddress = wInfo.getMacAddress();

                ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                Call<String> call = api.setFCMToken(new FCMInfo(macAddress, fcmToken));

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            if (response.body().equalsIgnoreCase("success")) {
                                context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean("fcm_token_status", true).apply();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void showDialog(final Context context, String title, String message){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
//TODO Barry Alien click