package com.example.intern.ptp.FCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.NotificationResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService {

    /**
     * handle message received from Firebase server
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        sendNotification(data.get("message"));

        Bus bus = BusManager.getBus();
        bus.post(new NotificationResponse(NotificationResponse.MESSAGE_RECEIVED, message));
     }

    /**
     * notify an notification on the phone of a user has logged in
     */
    private void sendNotification(String message) {
        try {
            // if no user has logged in, do nothing
            String username = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");
            if (username.equalsIgnoreCase("")) {
                return;
            }
            // user Gson to get Alert information from json-string message
            Gson gson = new Gson();
            Alert alert = gson.fromJson(message, Alert.class);

            // get resident id
            int resident_id = Integer.parseInt(alert.getResidentId());

            // get notification id
            int notification_id = Integer.parseInt(alert.getId());

            // get firstname of the resident
            String name = alert.getFirstname();

            // prepare content for the notification
            String content = "Resident " + name;
            boolean ok = !alert.getOk().equalsIgnoreCase("0");
            if (ok)
                content += " has been taken care of.";
            else
                content += " needs your help now!";

            Intent intent = new Intent(MyFcmListenerService.this, ResidentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Preferences.resident_idTag, alert.getResidentId());

            // create pendding intent for the notification
            PendingIntent pendingIntent = PendingIntent.getActivity(MyFcmListenerService.this, notification_id, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            // set sound for the notification
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // build a notification with icon, title, content, sound, ...
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_bell)
                    .setContentTitle("Resident - " + name)
                    .setColor(ok ? Color.BLUE : Color.RED)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);


            // notify the built notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Preferences.notify_idTag, resident_id, notificationBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
