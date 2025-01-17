package com.example.intern.ptp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.intern.ptp.network.client.AuthenticationClient;
import com.example.intern.ptp.utils.ConnectivityUtils;
import com.example.intern.ptp.utils.PlayServiceUtils;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Subscribe;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ConnectivityUtils.isConnected(this)) {
            ConnectivityUtils.showConnectionFailureDialog(this);
            return;
        }

        // Check Google Play Services plug-in to be able to register to FCM server and receive message from it
        PlayServiceUtils.checkPlayServices(this);

        // get Shared Preferences of the app
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        // get session token from the Shared Preferences
        String token = pref.getString("token", "");

        // if a token is available then try to login
        if (!token.equalsIgnoreCase("")) {
            AuthenticationClient service = AuthenticationClient.getClient();
            service.checkToken(this);
        } else {
            startLogin();
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.POST_CHECK_TOKEN)) {
            boolean isTokenValid = (Boolean) event.getMessage();

            if (isTokenValid) {
                startAuthenticatedArea();
            } else {
                startLogin();
            }
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {
            startLogin();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAuthenticatedArea() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 0);
        finish();
    }
}