package com.example.intern.ptp.Alert;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.Resident.ResidentActivity2;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertActivity extends Activity {
    @BindView(R.id.take_action)
    Button bt;

    @BindView(R.id.message)
    TextView mes;

    @BindView(R.id.addition)
    TextView tv;

    @BindView(R.id.resident)
    TextView resident;

    private ServerApi api;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);

        try {
            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get notification information
            Call<List<Alert>> call = api.getAlerts(activity.getIntent().getStringExtra(Preferences.notify_idTag), "all");
            call.enqueue(new Callback<List<Alert>>() {
                @Override
                public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                    try {
                        // if exception occurs or inconsistent database in server
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, "Server Error", "Please try again !");
                            return;
                        }

                        // if session is expired
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(activity);
                            return;
                        }
                        final Alert alert = response.body().get(0);
                        resident.setText(alert.getFirstname() + " " + alert.getLastname());
                        resident.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    // create an API service and set session token to request header
                                    api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                    // create request object to check session timeout
                                    Call<ResponseBody> call = api.getCheck();
                                    Preferences.showLoading(activity);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                // if exception occurs or inconsistent database in server
                                                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                    Preferences.dismissLoading();
                                                    Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                    return;
                                                }

                                                // if session is expired
                                                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                    Preferences.goLogin(activity);
                                                    return;
                                                }

                                                // create a new intent related to ResidentActivity
                                                Intent intent = new Intent(activity, ResidentActivity2.class);

                                                // put resident id of the notification as an extra in the above created intent
                                                intent.putExtra(Preferences.resident_idTag, alert.getResidentId());

                                                // start a new ResidentActivity with the intent
                                                startActivity(intent);
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
                                } catch (Exception e) {
                                    Preferences.dismissLoading();
                                    e.printStackTrace();
                                }
                            }
                        });
                        bt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    // create an API service and set session token to request header
                                    api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                    // create request object to send a take-care-action request to server
                                    Call<String> call = api.setTakecare(alert.getId(), activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
                                    Preferences.showLoading(activity);
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                // if exception occurs or inconsistent database in server
                                                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                    Preferences.dismissLoading();
                                                    Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                    return;
                                                }

                                                // if session is expired
                                                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                    Preferences.goLogin(activity);
                                                    return;
                                                }

                                                // get response from server
                                                String res = response.body();

                                                // if successfully sent take-care-action request to server
                                                if (res.equalsIgnoreCase("success")) {
                                                    bt.setEnabled(false);
                                                    mes.setText("HAS BEEN TAKEN CARE OF");
                                                    mes.setTextColor(Color.BLUE);
                                                    tv.setText("By " + activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
                                                } else {
                                                    // if the notification is already taken care of by another user
                                                    if (!res.equalsIgnoreCase("failed")) {
                                                        bt.setEnabled(false);
                                                        mes.setText("HAS BEEN TAKEN CARE OF");
                                                        mes.setTextColor(Color.BLUE);
                                                        tv.setText("By " + res);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            Preferences.dismissLoading();
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Preferences.dismissLoading();
                                            t.printStackTrace();
                                            Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                                        }
                                    });
                                } catch (Exception e) {
                                    Preferences.dismissLoading();
                                    e.printStackTrace();
                                }
                            }
                        });
                        // check whether the notification has been taken care of or not to display suitable information
                        if (alert.getOk().equalsIgnoreCase("0")) {
                            bt.setEnabled(true);
                            mes.setText("NEEDS YOUR HELP NOW!");
                            mes.setTextColor(Color.RED);
                            tv.setText("Last position: " + alert.getLastPosition());
                        } else {
                            bt.setEnabled(false);
                            mes.setText("HAS BEEN TAKEN CARE OF");
                            mes.setTextColor(Color.BLUE);
                            tv.setText("By " + alert.getUsername());
                        }
                        Preferences.dismissLoading();
                    } catch (Exception e) {
                        Preferences.dismissLoading();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<List<Alert>> call, Throwable t) {
                    Preferences.dismissLoading();
                    t.printStackTrace();
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                }
            });


        } catch (Exception e) {
            Preferences.dismissLoading();
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.menu_activity_alert, menu);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                // display predefined title for action bar
                actionBar.setDisplayShowTitleEnabled(true);

                // display go-back-home arrow at the left most of the action bar
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            int id = item.getItemId();

            switch (id) {
                case android.R.id.home:
                    // app icon in action bar clicked; goto parent activity.
                    activity.finish();
                    return true;

                // reload activity
                case R.id.action_refresh_alert:
                    activity.recreate();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}