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
import android.widget.ListView;
import android.widget.Toast;

import com.example.intern.ptp.MapActivity;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.client.MapClient;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.response.NotificationMessage;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.example.intern.ptp.views.adapter.MapListAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MapListFragment extends BaseFragment {

    @BindView(R.id.content_view)
    ListView mapListView;

    private MapListAdapter adapter;
    private Activity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        // check whether the device has successfully sent a registered FCM token to server, if not and the FCM token is available then send it
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
            inflater.inflate(R.menu.menu_fragement_maplist, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and search it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_map));
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
        return inflater.inflate(R.layout.fragment_maplist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MapListAdapter(activity, new ArrayList<Location>());
        mapListView.setAdapter(adapter);
        mapListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, MapActivity.class);
                Location location = (Location) adapter.getItem(position);

                intent.putExtra(Preferences.floorFilePathTag, location.getFilePath());
                intent.putExtra(Preferences.floor_idTag, location.getId());
                intent.putExtra(Preferences.floor_labelTag, location.getLabel());
                activity.startActivity(intent);
            }
        });

        refreshView();
    }

    private void refreshView() {
        showProgress(adapter.getCount() == 0);

        MapClient service = MapClient.getClient();
        service.getFloors(getActivity());
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_FLOOR_LIST)) {
            List<Location> locations = (List<Location>) event.getMessage();
            adapter.updateLocations(locations);

            showContent();
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {

            if (adapter.getCount() > 0) {
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
}

