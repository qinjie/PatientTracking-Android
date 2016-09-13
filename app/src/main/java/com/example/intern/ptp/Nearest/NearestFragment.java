package com.example.intern.ptp.Nearest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NearestFragment extends Fragment {

    @BindView(R.id.tvResident)
    TextView tvResident;

    @BindView(R.id.tvDistance)
    TextView tvDistance;

    private Activity activity;
    private ServerApi api;
    private String username;
    private boolean red = true;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            try {
                // get result with Preferences.nearest_resultTag from NearestService's broadcast intent
                String result = intent.getStringExtra(Preferences.nearest_resultTag);

                // if exception occurs or inconsistent database in server
                if (result.equalsIgnoreCase("failed")) {
                    Preferences.kill(activity, ":nearestservice");
                    Preferences.showDialog(context, "Server Error", "Please try again!");
                    return;
                }

                // if connection is failed
                if (result.equalsIgnoreCase("connection_failure")) {
                    Preferences.kill(activity, ":nearestservice");
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                    return;
                }

                // if session is expired
                if (!result.equalsIgnoreCase("isNotExpired")) {
                    Preferences.goLogin(context);
                    return;
                }

                // if successfully receive the nearest resident from server
                final Resident resident = intent.getParcelableExtra(Preferences.nearest_residentTag);
                if (resident != null) {
                    tvResident.setText(resident.getFirstname() + " " + resident.getLastname());
                    if (resident.getId() != null) {
                        tvResident.setOnClickListener(new View.OnClickListener() {
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
                                                Intent intent = new Intent(activity, ResidentActivity.class);

                                                // put resident id as an extra in the above created intent
                                                intent.putExtra(Preferences.resident_idTag, resident.getId());

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
                    }
                    tvDistance.setText(resident.getDistance());
                    if (red) {
                        tvResident.setTextColor(Color.RED);
                        tvDistance.setTextColor(Color.RED);
                    } else {
                        tvResident.setTextColor(Color.BLUE);
                        tvDistance.setTextColor(Color.BLUE);
                    }
                    red = !red;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public NearestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        // check whether the device has successfully sent a registered FCM token to server, if not and the FCM token is available then send it
        Preferences.checkFcmTokenAndFirstLoginAlertStatus(activity);
        username = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        try {
            inflater.inflate(R.menu.menu_fragment_nearest, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and display it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_neasrest_resident));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            int id = item.getItemId();

            switch (id) {
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_nearest, container, false);
        ButterKnife.bind(this, myView);

        try {
            // register a broadcast receiver with the tag equals "Preferences.nearest_broadcastTag + username"
            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.nearest_broadcastTag + username));

            // start a new NearestService
            activity.startService(new Intent(activity, NearestService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myView;
    }

    public void onResume() {
        super.onResume();
        try {
            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.nearest_broadcastTag + username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Preferences.kill(activity, ":nearestservice");
        try {
            activity.unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        try {
            activity.unregisterReceiver(mMessageReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Preferences.kill(activity, ":nearestservice");
        super.onDetach();
    }

}
