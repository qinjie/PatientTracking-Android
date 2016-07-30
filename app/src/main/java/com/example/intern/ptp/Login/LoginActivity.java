package com.example.intern.ptp.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.intern.ptp.Navigation.NavigationActivity;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private EditText mUsernameView, mPasswordView;
    private Button mButton;
    private String username, password, token, MAC;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ServerApi api;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            checkPlayServices();
            pref = getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

            token = pref.getString("token", "");
            if (!token.equalsIgnoreCase("")) {

                api = ServiceGenerator.createService(ServerApi.class, this.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                Call<ResponseBody> call = api.getCheck();
                Preferences.showLoading(activity);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                Intent intent = new Intent(activity, NavigationActivity.class);
                                startActivity(intent);
                            } else {
                                Preferences.dismissLoading();
                            }
                        }catch (Exception e){
                            Preferences.dismissLoading();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Preferences.dismissLoading();
                        t.printStackTrace();
                    }
                });

            }
            setLayout();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLayout() {

        setContentView(R.layout.activity_login);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        username = pref.getString("username", "");
        password = pref.getString("password", "");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        MAC = wInfo.getMacAddress();

        mUsernameView.setText(username);
        mPasswordView.setText(password);

    }

    public void attemptLogin() {

        try {
            api = ServiceGenerator.createService(ServerApi.class);

            username = mUsernameView.getText().toString();
            if(username.isEmpty()){
                Preferences.showDialog(activity, null, "Please enter your username!");
                return;
            }
            password = mPasswordView.getText().toString();
            if(password.isEmpty()){
                Preferences.showDialog(activity, null, "Please enter your password!");
                return;
            }

            Call<LoginResult> call = api.getLogin(new LoginInfo(username, password, MAC));
            Preferences.showLoading(activity);

            call.enqueue(new Callback<LoginResult>() {
                @Override
                public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                    try {
                        LoginResult res = response.body();
                        if (res.getResult().equalsIgnoreCase("correct")) {
                            editor = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit();
                            editor.putString("token", res.getToken());
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();

                            Intent intent = new Intent(activity, NavigationActivity.class);
                            startActivity(intent);
                        } else if (res.getResult().equalsIgnoreCase("wrong")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, null, "Wrong username or password!");
                        }else{
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, "Server Error", "Please try again!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<LoginResult> call, Throwable t) {
                    try {
                        Preferences.dismissLoading();
                        t.printStackTrace();
                        Preferences.showDialog(activity, "Connection Failure", "Please check your network!");
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

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                this.finish();
            }
            return false;
        }
        return true;
    }
}

