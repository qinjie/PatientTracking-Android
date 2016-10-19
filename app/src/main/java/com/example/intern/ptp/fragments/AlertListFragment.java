package com.example.intern.ptp.fragments;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.Toast;

import com.example.intern.ptp.R;
import com.example.intern.ptp.ResidentActivity;
import com.example.intern.ptp.network.client.AlertClient;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.UserManager;
import com.example.intern.ptp.utils.bus.response.NotificationMessage;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.example.intern.ptp.views.adapter.AlertListAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AlertListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.alertlist_list)
    ListView alertListView;

    @BindView(R.id.alertlist_ongoing_only)
    CheckBox redCheck;

    private AlertListAdapter adapter;

    private Activity activity;


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
        super.onCreateOptionsMenu(menu, inflater);

        try {
            inflater.inflate(R.menu.menu_fragment_alert, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and refreshView it
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
                case R.id.action_refresh:
                    refreshView();
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

        return inflater.inflate(R.layout.fragment_alertlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AlertListAdapter(activity, new ArrayList<Alert>());
        alertListView.setAdapter(adapter);
        alertListView.setOnItemClickListener(this);

        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshView();
    }

    /**
     * retrieve status of RED only checkbox from Shared Preferences in order to refreshView user's preferred data
     */
    private void refreshView() {
        final AlertClient service = AlertClient.getClient();

        showProgress(adapter.getCount() == 0);

        // check whether user prefers only untaken care notifications
        if (!activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("red_only", "0").equalsIgnoreCase("0")) {
            redCheck.setChecked(true);
            service.getAlerts(getActivity(), true);
        } else {
            redCheck.setChecked(false);
            service.getAlerts(getActivity(), false);
        }

        // handle check event of the RED only check box
        redCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                service.getAlerts(getActivity(), isChecked);
            }
        });
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, View view, final int position, long id) {
        long viewId = view.getId();

        if (viewId == R.id.alerts_alert_take_care_button) {
            final Alert alert = (Alert) adapter.getItem(position);

            AlertClient service = AlertClient.getClient();
            service.postTakeCare(getActivity(), alert, UserManager.getName(getActivity()));
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
            onTakeCare((Alert) event.getMessage());
        } else if (event.getType().equals(ServerResponse.GET_ALERTS)) {
            onAlertsRefresh(event.getMessage());
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {

            if(adapter.getCount() > 0) {
                showContent();
                Toast.makeText(getActivity(), R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
            } else {
                showError(getString(R.string.error_unknown_server_error));
            }
        }
    }

    @Subscribe
    public void onNotificationResponse(NotificationMessage event) {
        if (event.getType().equals(NotificationMessage.MESSAGE_RECEIVED)) {
            refreshView();
        }
    }

    private void onAlertsRefresh(Object response) {
        showContent();

        if (response instanceof List) {
            List<Alert> alertList = (List<Alert>) response;
            adapter.updateAlerts(alertList);
        }
    }

    private void onTakeCare(Alert alert) {
        if (!alert.isOngoing()) {
            alert.setUserId(UserManager.getId(getActivity()));
            alert.setUsername(UserManager.getName(getActivity()));
        }

        adapter.updateAlerts();
    }
}
