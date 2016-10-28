package com.example.intern.ptp.fragments;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.Toast;

import com.example.intern.ptp.R;
import com.example.intern.ptp.ResidentActivity;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.services.NearestService;
import com.example.intern.ptp.utils.Preferences;

import butterknife.BindView;

public class NearestResidentFragment extends BaseFragment {

    @BindView(R.id.tvResident)
    TextView tvResident;

    @BindView(R.id.tvDistance)
    TextView tvDistance;

    private Activity activity;
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
                    Toast.makeText(getActivity(), R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                // if connection is failed
                if (result.equalsIgnoreCase("connection_failure")) {
                    Preferences.kill(activity, ":nearestservice");
                    Toast.makeText(getActivity(), R.string.error_connection_failure, Toast.LENGTH_SHORT).show();
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
                                Intent intent = new Intent(activity, ResidentActivity.class);
                                intent.putExtra(Preferences.resident_idTag, resident.getId());
                                startActivity(intent);
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

    public NearestResidentFragment() {
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
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and search it
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
        return inflater.inflate(R.layout.fragment_nearest, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // register a broadcast receiver with the tag equals "Preferences.nearest_broadcastTag + username"
            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.nearest_broadcastTag + username));

            // start a new NearestService
            activity.startService(new Intent(activity, NearestService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
