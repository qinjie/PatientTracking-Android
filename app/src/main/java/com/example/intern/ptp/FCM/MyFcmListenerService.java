package com.example.intern.ptp.FCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.intern.ptp.Alert.AlertActivity;
import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message){
        Map<String, String> data = message.getData();
        sendNotification(data.get("message"));
    }

    private void sendNotification(String message) {
        try {
            Gson gson = new Gson();
            Alert alert = gson.fromJson(message, Alert.class);
            int resident_id = Integer.parseInt(alert.getResidentId());

            String name = alert.getFirstname();

            String content = "Resident " + name;
            boolean ok = !alert.getOk().equalsIgnoreCase("0");
            if (ok)
                content += " has been taken care of.";
            else
                content += " needs your help now!";

            Intent intent = new Intent(MyFcmListenerService.this, AlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Preferences.notify_Tag, alert.getId());


            PendingIntent pendingIntent = PendingIntent.getActivity(MyFcmListenerService.this, Preferences.requestCode++ , intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_sos)
                    .setContentTitle("PTP Alert " + resident_id)
                    .setColor(ok ? Color.BLUE : Color.RED)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);


            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(Preferences.notify_Tag, resident_id, notificationBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
