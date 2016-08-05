package com.example.intern.ptp.Location;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends Activity {
    private TextView tvID, tvLabel, tvDescription, tvWidth, tvHeight, tvCount;
    private String mID, mLabel, mDescription, mWidth, mHeight, mCount;

    private ServerApi api;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_location);
            tvID = (TextView) findViewById(R.id.ID);
            tvLabel = (TextView) findViewById(R.id.label);
            tvDescription = (TextView) findViewById(R.id.description);
            tvWidth = (TextView) findViewById(R.id.width);
            tvHeight = (TextView) findViewById(R.id.height);
            tvCount = (TextView) findViewById(R.id.count);

            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, this.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get a floor information
            Call<List<Location>> call = api.getFloor(this.getIntent().getStringExtra(Preferences.floor_idTag));
            call.enqueue(new Callback<List<Location>>() {
                @Override
                public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
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

                        // get response data from server
                        List<Location> res = response.body();
                        Location obj = res.get(0);

                        // get value for each data field
                        mID = obj.getId();
                        mLabel = obj.getLabel();
                        mDescription = obj.getDescription();
                        mWidth = obj.getWidth();
                        mHeight = obj.getHeight();
                        mCount = obj.getCount();

                        // display data fields
                        tvID.setText(mID);
                        tvLabel.setText(mLabel);
                        tvDescription.setText(mDescription);
                        tvWidth.setText(mWidth);
                        tvHeight.setText(mHeight);
                        tvCount.setText(mCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Preferences.dismissLoading();
                }

                @Override
                public void onFailure(Call<List<Location>> call, Throwable t) {
                    Preferences.dismissLoading();
                    t.printStackTrace();
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.menu_activity_location, menu);
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
                case R.id.action_refresh_location:
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
