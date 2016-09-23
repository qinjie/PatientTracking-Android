package com.example.intern.ptp.Alert;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.ResidentActivity2;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertFragment extends Fragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.nListView)
    ListView alertListView;

    @BindView(R.id.redCheck)
    CheckBox redCheck;

    private AlertListAdapter adapter;

    private Activity activity;
    private ServerApi api;

    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        // check whether the device has successfully sent a registered FCM token to server, if not and a FCM token is available then send it
        Preferences.checkFcmTokenAndFirstLoginAlertStatus(activity);
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
            inflater.inflate(R.menu.menu_fragment_alert, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and display it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_alert));
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
                // reload fragment
                case R.id.action_refresh_fragment_alert:
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert, container, false);
        ButterKnife.bind(this, view);

        // assign data contained in alertList to alertListView
        adapter = new AlertListAdapter(activity, new ArrayList<Alert>());
        alertListView.setAdapter(adapter);
        // handle click event on each list view item
        alertListView.setOnItemClickListener(this);

        // display a list of notification basing on checked status of the above check box
        display();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // refresh the notification list after coming back from AlertActivity
        display();
    }

    /**
     * retrieve status of RED only checkbox from Shared Preferences in order to display user's preferred data
     */
    private void display() {

        // check whether user prefers only untaken care notificaiotns
        if (!activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("red_only", "0").equalsIgnoreCase("0")) {
            redCheck.setChecked(true);
            getData("0");
        } else {
            redCheck.setChecked(false);
            getData("all");
        }


        // handle check event of the RED only check box
        redCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    // create an API service and set session token to request header
                    api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                    // create request object to check session time out
                    Call<ResponseBody> call = api.getCheck();
                    Preferences.showLoading(activity);
                    final boolean isC = isChecked;
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

                                // save status of RED only checkbox to Shared Preferences
                                activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).edit().putString("red_only", isC ? "1" : "0").apply();

                                // display preferred data
                                if (isC) {
                                    getData("0");
                                } else {
                                    getData("all");
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
                } catch (Exception e) {
                    Preferences.dismissLoading();
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * display notifications on list view basing on 'ok' parameter
     *
     * @param ok 1. "all": get all notifications from database and display on list view
     *           2. otherwise get untaken care notifications from database and display on list view
     */
    private void getData(String ok) {
        try {
            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get notification information
            Call<List<Alert>> call = api.getAlerts("all", ok);
            Preferences.showLoading(activity);
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

                        // get list of Alerts from response
                        List<Alert> alertList = response.body();
                        adapter.updateAlerts(alertList);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Preferences.dismissLoading();
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
    public void onItemClick(final AdapterView<?> adapterView, View view, final int position, long id) {
        long viewId = view.getId();

        if (viewId == R.id.alerts_alert_take_care_button) {
            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
            final Alert alert = (Alert) adapter.getItem(position);

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
                        if (res.equalsIgnoreCase("success") || !res.equalsIgnoreCase("failed")) {
                            alert.setOk("1");
                            adapter.updateAlerts();
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
        } else {

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

                            Alert alert = (Alert) adapter.getItem(position);

                            Intent intent = new Intent(activity, ResidentActivity2.class);
                            intent.putExtra(Preferences.BUNDLE_KEY_ALERT, alert);
                            intent.putExtra(Preferences.resident_idTag, alert.getResidentId());

                            activity.startActivity(intent);
                        } catch (Exception e) {
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
    }
}
