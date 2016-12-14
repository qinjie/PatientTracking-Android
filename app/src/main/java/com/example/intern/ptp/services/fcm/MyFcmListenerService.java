package com.example.intern.ptp.services.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.example.intern.ptp.R;
import com.example.intern.ptp.ResidentActivity;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.services.TakeCareService;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.UserManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.NotificationMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MyFcmListenerService extends FirebaseMessagingService {

    /**
     * Handle message received from Firebase server
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {

        if (UserManager.isLoggedIn(getApplicationContext())) {
            Map<String, String> data = message.getData();
            Alert alert = parseAlert(data.get("message"));

            if (alert == null) {
                return;
            }

            if (alert.isOngoing()) {
                sendNotification(alert);
            } else {
                cancelNotification(alert);
            }

            Bus bus = BusManager.getBus();
            bus.post(new NotificationMessage(NotificationMessage.MESSAGE_RECEIVED, alert));
        }
    }

    /**
     * Notify an notification on the phone of a user has logged in
     */
    private void sendNotification(Alert alert) {
        try {
            int residentId = Integer.parseInt(alert.getResidentId());
            int notificationId = Integer.parseInt(alert.getId());

            Intent openResidentIntent = new Intent(MyFcmListenerService.this, ResidentActivity.class);
            openResidentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openResidentIntent.putExtra(Preferences.RESIDENT_ID, alert.getResidentId());
            PendingIntent openResidentPendingIntent = PendingIntent.getActivity(MyFcmListenerService.this, notificationId, openResidentIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            Intent takeCareIntent = new Intent(MyFcmListenerService.this, TakeCareService.class);
            takeCareIntent.putExtra(Preferences.RESIDENT_ID, alert.getResidentId());
            takeCareIntent.putExtra(Preferences.BUNDLE_KEY_ALERT, alert);
            PendingIntent takeCarePendingIntent = PendingIntent.getService(this, notificationId, takeCareIntent, 0);

            String contentTitle = alert.getFirstname() + " " + alert.getLastname();
            List<String> alertTypes = Arrays.asList(getResources().getStringArray(R.array.alert_types));
            String content = alertTypes.get(Integer.parseInt(alert.getType()));
            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            int imageIdentifier = getResources().getIdentifier("profile" + residentId, "drawable", getPackageName());
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), imageIdentifier);

            int notificationColor = ContextCompat.getColor(getApplicationContext(), R.color.red);

            NotificationCompat.Builder phoneNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_bell)
                    .setContentTitle(contentTitle)
                    .setColor(notificationColor)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(notificationSound)
                    .setContentIntent(openResidentPendingIntent)
                    .setGroup(alert.getResidentId())
                    .setOngoing(true)
                    .addAction(R.drawable.ic_bell,
                            getString(R.string.take_care), takeCarePendingIntent);

            NotificationCompat.Builder wearNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_bell)
                    .setContentTitle(contentTitle)
                    .setColor(notificationColor)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .setSound(notificationSound)
                    .setContentIntent(openResidentPendingIntent)
                    .setGroup(alert.getResidentId())
                    .setOngoing(false).addAction(R.drawable.ic_bell,
                            getString(R.string.take_care), takeCarePendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(Preferences.notify_idTag, residentId, phoneNotificationBuilder.build());
            notificationManager.notify(Preferences.notify_idTag, residentId, wearNotificationBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancel a notification when the user or other users took care of the alert
     */
    private void cancelNotification(Alert alert) {
        int residentId = Integer.parseInt(alert.getResidentId());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Preferences.notify_idTag, residentId);
    }

    private Alert parseAlert(String message) {
        Gson gson = new Gson();
        return gson.fromJson(message, Alert.class);
    }
}
