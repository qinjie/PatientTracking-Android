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
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.network.rest.AlertService;
import com.example.intern.ptp.network.rest.ServerResponse;
import com.example.intern.ptp.utils.BusManager;
import com.example.intern.ptp.utils.UserManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        Bus bus = BusManager.getBus();
        bus.register(this);

        // check whether the device has successfully sent a registered FCM token to server, if not and a FCM token is available then send it
        Preferences.checkFcmTokenAndFirstLoginAlertStatus(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
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
        //super.onCreateOptionsMenu(menu, inflater);
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
                    display();
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

        // refresh the notification list after coming back from ResidentFragment
        display();
    }

    /**
     * retrieve status of RED only checkbox from Shared Preferences in order to display user's preferred data
     */
    private void display() {
        final AlertService service = AlertService.getService();

        // check whether user prefers only untaken care notificaiotns
        if (!activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("red_only", "0").equalsIgnoreCase("0")) {
            redCheck.setChecked(true);
            service.getAlerts(true);
        } else {
            redCheck.setChecked(false);
            service.getAlerts(false);
        }


        // handle check event of the RED only check box
        redCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                service.getAlerts(isChecked);

            }
        });
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, View view, final int position, long id) {
        long viewId = view.getId();

        if (viewId == R.id.alerts_alert_take_care_button) {
            // create an API service and set session token to request header
            ServerApi api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
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
                            alert.setUserId(UserManager.getId(getActivity()));
                            alert.setUsername(UserManager.getName(getActivity()));
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
            Alert alert = (Alert) adapter.getItem(position);

            Intent intent = new Intent(activity, ResidentActivity.class);
            intent.putExtra(Preferences.BUNDLE_KEY_ALERT, alert);
            intent.putExtra(Preferences.resident_idTag, alert.getResidentId());

            activity.startActivity(intent);
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.POST_TAKE_CARE)) {
            onTakeCare((String) event.getResponse());
        } else if (event.getType().equals(ServerResponse.GET_ALERTS)) {
            onAlertsRefresh(event.getResponse());
        }
    }

    private void onAlertsRefresh(Object response) {
        if (response instanceof List) {
            List<Alert> alertList = (List<Alert>) response;
            adapter.updateAlerts(alertList);
        }
    }

    private void onTakeCare(String success) {

    }
}
