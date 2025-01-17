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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.intern.ptp.R;
import com.example.intern.ptp.ResidentActivity;
import com.example.intern.ptp.network.client.MapClient;
import com.example.intern.ptp.network.client.ResidentClient;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.network.models.SearchParam;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.example.intern.ptp.views.adapter.ResidentListAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ResidentListFragment extends BaseFragment {
    private SearchView sv;

    @BindView(R.id.resident_list)
    ListView residentList;

    @BindView(R.id.resident_map_spinner)
    Spinner mapSpinner;

    private Activity activity;

    private ArrayAdapter<Location> floorAdapter;

    /**
     * search resident by a SearchParam and search result on a table namely resident list table
     */
    public void search(SearchParam param) {
        try {
            ResidentClient service = ResidentClient.getClient();
            service.listResidents(activity, param);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
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
            case R.id.action_refresh:
                refreshView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_residentlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        residentList.setAdapter(new ResidentListAdapter(getActivity(), new ArrayList<Resident>()));
        residentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ResidentListAdapter adapter = (ResidentListAdapter) adapterView.getAdapter();
                Resident resident = (Resident) adapter.getItem(position);

                Intent intent = new Intent(activity, ResidentActivity.class);
                intent.putExtra(Preferences.RESIDENT_ID, resident.getId());
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
    }

    private void refreshView() {
        showProgress(floorAdapter.getCount() == 0);

        MapClient service = MapClient.getClient();
        service.getFloors(getActivity());
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_RESIDENT_LIST)) {
            List<Resident> residents = (List<Resident>) event.getMessage();
            updateResidents(residents);
        } else if (event.getType().equals(ServerResponse.GET_FLOOR_LIST)) {
            List<Location> locations = (List<Location>) event.getMessage();
            updateFloors(locations);
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {

            if (floorAdapter.getCount() > 0) {
                Toast.makeText(getActivity(), R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
                showContent();
            } else {
                showError(getString(R.string.error_unknown_server_error));
            }
        }
    }

    private void updateResidents(List<Resident> residents) {
        ResidentListAdapter adapter = (ResidentListAdapter) residentList.getAdapter();
        adapter.setResidents(residents);

        showContent();
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

}
