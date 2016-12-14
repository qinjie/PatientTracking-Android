package com.example.intern.ptp.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.intern.ptp.R;
import com.example.intern.ptp.network.client.AlertClient;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.UserManager;

public class TakeCareService extends IntentService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public TakeCareService() {
        super("TakeCareService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TakeCareService", "onStartCommand");

        Alert alert = intent.getParcelableExtra(Preferences.BUNDLE_KEY_ALERT);

        if (alert != null) {
            AlertClient client = AlertClient.getClient();
            String name = UserManager.getName(this);

            client.postTakeCare(this, alert, name);

        }

        return START_NOT_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
