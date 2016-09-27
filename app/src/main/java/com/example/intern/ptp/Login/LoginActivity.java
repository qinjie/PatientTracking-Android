package com.example.intern.ptp.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.intern.ptp.Navigation.NavigationActivity;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.security.AccessController.getContext;

public class LoginActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @BindView(R.id.username)
    EditText mUsernameView;

    @BindView(R.id.password)
    EditText mPasswordView;

    private String username, password, token, MAC;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ServerApi api;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // check Google Play Services plug-in to be able to register to FCM server and receive message from it
            checkPlayServices();

            // get Shared Preferences of the app
            pref = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

            // get session token from the Shared Preferences
            token = pref.getString("token", "");

            // if a token is available then try to login
            if (!token.equalsIgnoreCase("")) {

                // create an API service and set session token to request header
                api = ServiceGenerator.createService(ServerApi.class, this.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                // create request object to check session timeout
                Call<ResponseBody> call = api.getCheck();
                Preferences.showLoading(activity);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            // if session is not expired
                            if (response.headers().get("result").equalsIgnoreCase("isNotExpired")) {

                                // start NavigationActivity
                                Intent intent = new Intent(activity, NavigationActivity.class);
                                startActivityForResult(intent, 0);
                            } else {
                                Preferences.dismissLoading();
                            }
                        } catch (Exception e) {
                            Preferences.dismissLoading();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Preferences.dismissLoading();
                        t.printStackTrace();
                        Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                    }
                });

            }

            // set layout for the activity
            setLayout();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set layout for the activity
     */
    public void setLayout() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

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
    @OnClick(R.id.button)
    public void attemptLogin() {

        try {
            // create an API service
            api = ServiceGenerator.createService(ServerApi.class);

            // get input username
            username = mUsernameView.getText().toString();

            // empty username is not allowed
            if (username.isEmpty()) {
                Preferences.showDialog(activity, null, "Please enter your username!");
                return;
            }

            // get input password
            password = mPasswordView.getText().toString();

            // empty password is not allowed
            if (password.isEmpty()) {
                Preferences.showDialog(activity, null, "Please enter your password!");
                return;
            }

            // create request object to send login information
            Call<LoginResult> call = api.getLogin(new LoginInfo(username, password, MAC));
            Preferences.showLoading(activity);
            call.enqueue(new Callback<LoginResult>() {
                @Override
                public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                    try {
                        // get response information from server
                        LoginResult res = response.body();

                        // if username and password are correct
                        if (res.getResult().equalsIgnoreCase("correct")) {

                            editor = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit();
                            editor.putString("id", res.getUserId());
                            editor.putString("token", res.getToken());
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putString("email", res.getEmail());
                            editor.apply();

                            // start NavigationActivity
                            Intent intent = new Intent(activity, NavigationActivity.class);
                            startActivityForResult(intent, 0);

                            // if username or password is wrong, notify user by a dialog
                        } else if (res.getResult().equalsIgnoreCase("wrong")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, null, "Wrong username or password!");


                            // otherwise exception occurs or inconsistent database in server
                        } else {
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, "Server Error", "Please try again!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // if connection to server is failed
                @Override
                public void onFailure(Call<LoginResult> call, Throwable t) {
                    try {
                        Preferences.dismissLoading();
                        t.printStackTrace();
                        Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                    } catch (Exception e) {
                        Preferences.dismissLoading();
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check Google Play Services plug-in
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED) {
            finish();
        }
    }
}

