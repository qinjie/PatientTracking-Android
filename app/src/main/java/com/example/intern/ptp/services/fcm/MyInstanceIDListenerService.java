package com.example.intern.ptp.services.fcm;

import com.example.intern.ptp.utils.Preferences;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        saveToken(refreshedToken);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param FCM_token The new token.
     */
    private void saveToken(String FCM_token) {
        try {
            // save FCM token status and data to Shared Preferences
            this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putString(Preferences.FCM_tokenTag, FCM_token).apply();
            this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putBoolean(Preferences.FCM_token_statusTag, false).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}