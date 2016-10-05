package com.example.intern.ptp.Resident;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;

import com.example.intern.ptp.Map.Location;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.rest.MapService;
import com.example.intern.ptp.network.rest.ResidentService;
import com.example.intern.ptp.utils.ProgressManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResidentListFragment extends Fragment {
    private SearchView sv;

    @BindView(R.id.maplist_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.resident_content)
    View contentView;

    @BindView(R.id.resident_list)
    ListView residentList;

    @BindView(R.id.resident_map_spinner)
    Spinner mapSpinner;

    private Activity activity;

    private ArrayAdapter<Location> floorAdapter;

    private ProgressManager progressManager;

    /**
     * search resident by a SearchParam and search result on a table namely resident list table
     */
    public void search(SearchParam param) {
        try {
            ResidentService service = ResidentService.getService();
            service.listResidents(activity, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
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
            progressManager.initRefreshingIndicator(menu, R.id.action_refresh_fragment_residentlist);
            inflater.inflate(R.menu.menu_fragment_resident, menu);
            ActionBar actionBar = getActivity().getActionBar();
            if (actionBar != null) {
                // set title for action bar and search it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_resident));
            }

            MenuItem searchMenuItem = menu.findItem(R.id.search);
            sv = (SearchView) searchMenuItem.getActionView();
            sv.setQueryHint(getString(R.string.search_hint));


            searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem menuItem) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                    search(new SearchParam("", ((Location) mapSpinner.getSelectedItem()).getId(), "name", "asc"));
                    return true;
                }
            });

            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {
                    search(new SearchParam(query.trim(), ((Location) mapSpinner.getSelectedItem()).getId(), "name", "asc"));
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return true;
                }
            });

            super.onCreateOptionsMenu(menu, inflater);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // reload fragment
            case R.id.action_refresh_fragment_residentlist:
                refreshView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_resident, container, false);
        ButterKnife.bind(this, myView);

        Bus bus = BusManager.getBus();
        bus.register(this);

        progressManager.initLoadingIndicator(contentView, progressIndicator);

        residentList.setAdapter(new ResidentListAdapter(getActivity(), new ArrayList<Resident>()));
        residentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ResidentListAdapter adapter = (ResidentListAdapter) adapterView.getAdapter();
                Resident resident = (Resident) adapter.getItem(position);

                Intent intent = new Intent(activity, ResidentActivity.class);
                intent.putExtra(Preferences.resident_idTag, resident.getId());
                activity.startActivity(intent);
            }
        });

        // assign data contained in locationList to mapSpinner
        floorAdapter = new ArrayAdapter<>(activity, R.layout.item_spinner, new ArrayList<Location>());
        floorAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        mapSpinner.setAdapter(floorAdapter);
        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // search sorted resident list as user's expectation basing on the order that user clicks the 3 headers: 'ID', 'Name', 'Location' in the resident list table
                search(new SearchParam(sv.getQuery().toString().trim(), ((Location) mapSpinner.getSelectedItem()).getId(), "name", "asc"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        refreshView();

        return myView;
    }

    private void refreshView() {
        progressManager.indicateProgress(floorAdapter.getCount() == 0);

        MapService service = MapService.getService();
        service.getFloors(getActivity());
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_RESIDENT_LIST)) {
            List<Resident> residents = (List<Resident>) event.getResponse();
            updateResidents(residents);
        } else if(event.getType().equals(ServerResponse.GET_FLOOR_LIST)) {
            List<Location> locations = (List<Location>) event.getResponse();
            updateFloors(locations);
        } else if (event.getType().equals(ServerResponse.SERVER_ERROR)) {
            Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
        }
    }

    private void updateResidents(List<Resident> residents) {
        ResidentListAdapter adapter = (ResidentListAdapter) residentList.getAdapter();
        adapter.setResidents(residents);

        progressManager.stopProgress();
    }

    private void updateFloors(List<Location> locations) {
        locations.add(0, new Location("all", "Show All Floors"));

        floorAdapter.clear();
        floorAdapter.addAll(locations);
        floorAdapter.notifyDataSetChanged();

        mapSpinner.setSelection(0);
        Location location = ((Location) mapSpinner.getSelectedItem());

        search(new SearchParam("", location.getId(), "name", "asc"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }
}
