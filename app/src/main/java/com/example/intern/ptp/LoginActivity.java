package com.example.intern.ptp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.intern.ptp.views.navigation.NavigationActivity;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.network.client.AuthenticationService;
import com.example.intern.ptp.network.models.LoginInfo;
import com.example.intern.ptp.network.models.LoginResult;
import com.example.intern.ptp.utils.ProgressManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        progressManager = new ProgressManager();
        progressManager.initLoadingIndicator(contentView, progressIndicator);

        Bus bus = BusManager.getBus();
        bus.register(this);

        // check Google Play Services plug-in to be able to register to FCM server and receive message from it
        checkPlayServices();

        // get Shared Preferences of the app
        pref = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        // get session token from the Shared Preferences
        String token = pref.getString("token", "");

        // if a token is available then try to login
        if (!token.equalsIgnoreCase("")) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            AuthenticationService service = AuthenticationService.getService();
            service.checkToken(this);
        } else {
            progressManager.stopProgress();
        }

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
        progressIndicator.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.INVISIBLE);

        // get input username
        username = mUsernameView.getText().toString();

        // empty username is not allowed
        if (username.isEmpty()) {
            Preferences.showDialog(this, null, "Please enter your username!");
            return;
        }

        // get input password
        password = mPasswordView.getText().toString();

        // empty password is not allowed
        if (password.isEmpty()) {
            Preferences.showDialog(this, null, "Please enter your password!");
            return;
        }

        AuthenticationService service = AuthenticationService.getService();
        service.login(this, new LoginInfo(username, password, MAC));
    }

    /**
     * check Google Play Services plug-in
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                this.finish();
            }
            return false;
        }
        return true;
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

        } else {
            if (result.getResult().equalsIgnoreCase("wrong")) {
                Preferences.showDialog(this, null, "Wrong username or password!");
            } else {
                Preferences.showDialog(this, "Server Error", "Please try again!");
            }
        }

        progressManager.stopProgress();
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
            boolean isTokenValid = (Boolean) event.getResponse();

            if (isTokenValid) {
                Intent intent = new Intent(this, NavigationActivity.class);
                startActivityForResult(intent, 0);
            }

            progressManager.stopProgress();

        } else if (event.getType().equals(ServerResponse.POST_LOGIN)) {
            login((LoginResult) event.getResponse());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);

    }
}
