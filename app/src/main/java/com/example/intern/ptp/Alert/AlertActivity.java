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
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertActivity extends Activity {
    private Button bt;
    private TextView mes, tv, resident;
    private ServerApi api;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        resident = (TextView) findViewById(R.id.resident);
        mes = (TextView) findViewById(R.id.message);
        bt = (Button) findViewById(R.id.take_action);
        tv = (TextView) findViewById(R.id.addition);

        try {
            api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
            Call<List<Alert>> call = api.getAlerts(this.getIntent().getStringExtra(Preferences.notify_Tag), "all");
            call.enqueue(new Callback<List<Alert>>() {
                @Override
                public void onResponse(Call<List<Alert>> call, Response<List<Alert>> response) {
                    try {
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, "Server Error", "Please try again !");
                            return;
                        }
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
                                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                    Call<ResponseBody> call = api.getCheck();
                                    Preferences.showLoading(activity);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                    Preferences.dismissLoading();
                                                    Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                    return;
                                                }
                                                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                    Preferences.goLogin(activity);
                                                    return;
                                                }
                                                Intent intent = new Intent(activity, ResidentActivity.class);
                                                intent.putExtra(Preferences.resident_idTag, alert.getResidentId());
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

                                    api = ServiceGenerator.createService(ServerApi.class, getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                    Call<String> call = api.setTakecare(alert.getId(), getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
                                    Preferences.showLoading(activity);
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                    Preferences.dismissLoading();
                                                    Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                    return;
                                                }
                                                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                    Preferences.goLogin(AlertActivity.this);
                                                    return;
                                                }
                                                if (response.body().equalsIgnoreCase("success")) {
                                                    bt.setEnabled(false);
                                                    mes.setText("HAS BEEN TAKEN CARE OF");
                                                    mes.setTextColor(Color.BLUE);
                                                    tv.setText("By " + getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
                                                } else {
                                                    if (!response.body().equalsIgnoreCase("failed")) {
                                                        bt.setEnabled(false);
                                                        mes.setText("HAS BEEN TAKEN CARE OF");
                                                        mes.setTextColor(Color.BLUE);
                                                        tv.setText("By " + AlertActivity.this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
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
                                        }
                                    });
                                } catch (Exception e) {
                                    Preferences.dismissLoading();
                                    e.printStackTrace();
                                }
                            }
                        });
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
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }catch (Exception e){
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
                    this.finish();
                    return true;
                case R.id.action_refresh_alert:
                    activity.recreate();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}