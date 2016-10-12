package com.example.intern.ptp.fragments;

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
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.intern.ptp.MapActivity;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.client.MapClient;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.ProgressManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.NotificationResponse;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.example.intern.ptp.views.adapter.MapListAdapter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapListFragment extends Fragment {

    @BindView(R.id.maplist_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.maplist_content)
    ListView mapListView;

    private MapListAdapter adapter;
    private Activity activity;
    private ProgressManager progressManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        progressManager = new ProgressManager();

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
            progressManager.initRefreshingIndicator(menu, R.id.action_refresh_fragment_maplist);
            inflater.inflate(R.menu.menu_fragment_map, menu);
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
                case R.id.action_refresh_fragment_maplist:
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
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_maplist, container, false);
        ButterKnife.bind(this, myView);

        Bus bus = BusManager.getBus();
        bus.register(this);

        progressManager.initLoadingIndicator(mapListView, progressIndicator);

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
        return myView;
    }

    private void refreshView() {
        progressManager.indicateProgress(adapter.getCount() == 0);

        MapClient service = MapClient.getClient();
        service.getFloors(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_FLOOR_LIST)) {
            List<Location> locations = (List<Location>) event.getResponse();
            adapter.updateLocations(locations);

            progressManager.stopProgress();
        }
    }

    @Subscribe
    public void onNotificationRespone(NotificationResponse event) {
        if (event.getType().equals(NotificationResponse.MESSAGE_RECEIVED)) {
            refreshView();
        }
    }
}

