package com.example.intern.ptp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.intern.ptp.FCM.FCMInfo;
import com.example.intern.ptp.Login.LoginActivity;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import java.util.List;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Preferences {
    // base URL for API
    public static final String root = "http://128.199.209.227/patient-tracking-web/api/web/"; //server

    // base URL for images in server
    public static final String imageRoot = "http://128.199.209.227/patient-tracking-web/backend/web/";

    // tags for Shared Preferences to store and retrieve some piece of data from local
    public static final String SharedPreferencesTag = "Resident_Tracking_Preferences";
    public static final int SharedPreferences_ModeTag = Context.MODE_PRIVATE;

    // tags for storing FCM token status to make sure the FCM token related the a device sent successfully to server
    public static final String FCM_tokenTag = "Resident_Tracking.FCM_token";
    public static final String FCM_token_statusTag = "Resident_Tracking.FCM_token_status";

    // tag for sending resident id through intents
    public static final String resident_idTag = "Resident_Tracking.id";

    // tag for sending alert id through intents
    public static final String notify_idTag = "Resident_Tracking_notification";

    public static final String alertTag = "Alert";

    // tags for sending basic floor information through intents
    public static final String floor_idTag = "Resident_Tracking.floor_id";
    public static final String floor_labelTag = "Resident_Tracking.floor_label";
    public static final String floorFilePathTag = "Resident_Tracking.floor_file_path";

    // tags for nearest resident function
    public static final String nearest_broadcastTag = "Resident_Tracking.NEAREST";
    public static final String nearest_residentTag = "Resident_Tracking.nearest_resident";
    public static final String nearest_resultTag = "Resident_Tracking.nearest_result";
    public static final int nearest_request_period = 2000;

    // tags for map function
    public static final String map_broadcastTag = "Resident_Tracking.MAP";
    public static final String map_pointsTag = "Resident_Tracking.map_points";
    public static final String map_resultTag = "Resident_Tracking.map_result";
    public static final int map_request_period = 2000;

    // tag for checking whether the user has received all untaken care notification from server after the user logs in
    public static final String first_login_alert_statusTag = "Resident_Tracking.first_login_alert_status";

    // a common ProgressDialog for the app
    public static ProgressDialog loading;

    // whether the ProgressDialog is on screen
    public static boolean isShownLoading = false;

    // whether there is a dialog on screen
    public static boolean isDialog = false;

    /**
     * go to log in screen
     */
    public static void goLogin(final Context context) {
        try {
            // store FCM token status and data
            String fcmToken = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString(Preferences.FCM_tokenTag, "");
            boolean fcmTokenStatus = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getBoolean(Preferences.FCM_token_statusTag, false);

            // clear other data
            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().clear().apply();

            // save FCM token status and data to Shared Preferences again
            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putString(Preferences.FCM_tokenTag, fcmToken).apply();
            context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean(Preferences.FCM_token_statusTag, fcmTokenStatus).apply();

            // clear all alerts
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            // make sure the loading dialog off
            if (isShownLoading) {
                isShownLoading = false;
                loading.dismiss();
            }

            // start a new LoginActivity and remove all other activities in stack
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show loading dialog
     */
    public static void showLoading(final Context context) {
        try {
            if (!isShownLoading) {
                isShownLoading = true;
                loading = ProgressDialog.show(context, context.getString(R.string.loading_title), context.getString(R.string.loading_message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * turn off loading dialog
     */
    public static void dismissLoading() {
        try {
            if (isShownLoading) {
                isShownLoading = false;
                loading.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * kill a process
     */
    public static void kill(final Context context, String serviceName) {
        try {
            // get all processes belong to the app
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            String pricessName = context.getPackageName() + serviceName;

            // run a for loop
            for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {

                // kill the process if found
                if (next.processName.equals(pricessName)) {
                    android.os.Process.killProcess(next.pid);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check FCM token to make sure the FCM token related the a device sent successfully to server
     * and also make sure user has received all untaken care notification from server after login
     */
    public static void checkFcmTokenAndFirstLoginAlertStatus(final Context context) {
        try {
            // retrieve FCM token status and data from Shared Preferences
            String fcmToken = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString(Preferences.FCM_tokenTag, "");
            boolean fcmTokenStatus = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getBoolean(Preferences.FCM_token_statusTag, false);

            // if there is a registered FCM token
            if (!fcmToken.equalsIgnoreCase("")) {
                // get MAC address of the device
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wInfo = wifiManager.getConnectionInfo();
                final String macAddress = wInfo.getMacAddress();

                // if FCM token has been sent to server successfully
                if (fcmTokenStatus) {
                    // make sure user has received all untaken care notification from server after login
                    notifyUntakenCareAlerts(context, macAddress);
                    return;
                }

                // create an API service and set session token to request header
                ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                // create request object to send FCM token to server
                Call<String> call = api.setFCMToken(new FCMInfo(macAddress, fcmToken));
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            // if server successfully received FCM token
                            if (response.body().equalsIgnoreCase("success")) {
                                // update FCM token satus
                                context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean(Preferences.FCM_token_statusTag, true).apply();

                                // make sure user has received all untaken care notification from server after login
                                notifyUntakenCareAlerts(context, macAddress);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        t.printStackTrace();
                        Preferences.showDialog(context, "Connection Failure", "Please check your network and try again!");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * display a dialog with title and message parameters
     */
    public static void showDialog(final Context context, String title, String message) {
        try {
            if (isDialog) {
                isDialog = false;
                return;
            }
            isDialog = true;
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * request all untakencare alerts from server
     */
    private static void notifyUntakenCareAlerts(final Context context, String macAddress) {
        try {
            // check first login alert status
            boolean firstLoginAlertStatus = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getBoolean(Preferences.first_login_alert_statusTag, false);
            if (firstLoginAlertStatus)
                return;

            // create an API service and set session token to request header
            ServerApi api = ServiceGenerator.createService(ServerApi.class, context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to request all untaken care alerts from server
            Call<String> call = api.notifyUntakenCareAlerts(macAddress);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    // if received all untakencare alerts successfully
                    if (response.body().equalsIgnoreCase("success")) {
                        // update first login alert status
                        context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean(Preferences.first_login_alert_statusTag, true).apply();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    t.printStackTrace();
                    Preferences.showDialog(context, "Connection Failure", "Please check your network and try again!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}