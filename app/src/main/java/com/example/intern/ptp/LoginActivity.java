package com.example.intern.ptp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.intern.ptp.network.client.AuthenticationClient;
import com.example.intern.ptp.network.models.LoginInfo;
import com.example.intern.ptp.network.models.LoginResult;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.ProgressManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.example.intern.ptp.views.navigation.NavigationActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {
    @BindView(R.id.login_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.login_content)
    View contentView;

    @BindView(R.id.login_username)
    EditText mUsernameView;

    @BindView(R.id.login_password)
    EditText mPasswordView;

    private String username, password, MAC;
    private SharedPreferences pref;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        progressManager = new ProgressManager();
        progressManager.initLoadingIndicator(contentView, progressIndicator);

        Bus bus = BusManager.getBus();
        bus.register(this);

        // get Shared Preferences of the app
        pref = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        setLayout();
    }

    /**
     * set layout for the activity
     */
    public void setLayout() {
        // get username stored in the Shared Preferences
        username = pref.getString("username", "");

        // get password stored in the Shared Preferences
        password = pref.getString("password", "");

        // get MAC address of the device
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        MAC = wInfo.getMacAddress();

        // set username and password to the two text views that receive input username and password form user
        // in order to help user save time
        mUsernameView.setText(username);
        mPasswordView.setText(password);
    }

    /**
     * attempt to request a login to the app
     */
    @OnClick(R.id.login_submit)
    public void attemptLogin() {

        // get input username
        username = mUsernameView.getText().toString();

        // empty username is not allowed
        if (username.isEmpty()) {
            Toast.makeText(this, R.string.validation_username, Toast.LENGTH_SHORT).show();
            ;
            return;
        }

        // get input password
        password = mPasswordView.getText().toString();

        // empty password is not allowed
        if (password.isEmpty()) {
            Toast.makeText(this, R.string.validation_password, Toast.LENGTH_SHORT).show();
            return;
        }

        progressManager.indicateProgress(true);

        AuthenticationClient service = AuthenticationClient.getClient();
        service.login(this, new LoginInfo(username, password, MAC));
    }

    private void login(LoginResult result) {
        if (result.getResult().equalsIgnoreCase("correct")) {

            SharedPreferences.Editor editor = this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit();
            editor.putString("id", result.getUserId());
            editor.putString("token", result.getToken());
            editor.putString("username", username);
            editor.putString("password", password);
            editor.putString("email", result.getEmail());
            editor.apply();

            // start NavigationActivity
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivityForResult(intent, 0);

            progressManager.stopProgressDelayed(100);

        } else if (result.getResult().equalsIgnoreCase("wrong")) {
            Toast.makeText(this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
            progressManager.stopProgress();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.POST_CHECK_TOKEN)) {
            boolean isTokenValid = (Boolean) event.getMessage();

            if (isTokenValid) {
                Intent intent = new Intent(this, NavigationActivity.class);
                startActivityForResult(intent, 0);
            }

            progressManager.stopProgress();

        } else if (event.getType().equals(ServerResponse.POST_LOGIN)) {
            login((LoginResult) event.getMessage());
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {
            Toast.makeText(this, R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
            progressManager.stopProgress();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);

    }
}
