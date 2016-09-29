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

public class ResidentFragment extends Fragment {
    private SearchView sv;

    @BindView(R.id.resident_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.resident_content)
    View contentView;

    @BindView(R.id.resident_list)
    ListView residentList;

    @BindView(R.id.resident_map_spinner)
    Spinner mapSpinner;

    private List<Location> locationList;
    private int sortIndex = -1;
    private String[] colName = {"name", "floor_id"};
    private boolean asc[] = {true, true};
    private Activity activity;
    private ServerApi api;

    /**
     * search resident by a SearchParam and display result on a table namely resident list table
     */
    public void display(SearchParam param) {
        try {
            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get list of residents detected by the system within location timeout from server
            Call<List<Resident>> call = api.getSearch(param);
            call.enqueue(new Callback<List<Resident>>() {
                @Override
                public void onResponse(final Call<List<Resident>> call, Response<List<Resident>> response) {
                    try {
                        // if exception occurs or inconsistent database in server
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.showDialog(activity, "Server Error", "Please try again !");
                            return;
                        }

                        // if session is expired
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(activity);
                            return;
                        }

                        // get list of Residents from response
                        List<Resident> residents = response.body();

                        ResidentListAdapter adapter = (ResidentListAdapter) residentList.getAdapter();
                        adapter.setResidents(residents);

                        progressIndicator.setVisibility(View.INVISIBLE);
                        contentView.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<List<Resident>> call, Throwable t) {
                    t.printStackTrace();
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                }

            });

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
                // set title for action bar and display it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_resident));
            }

            // get SearchView in action bar
            sv = (SearchView) menu.findItem(R.id.search).getActionView();

            // set hint for the SearchView
            sv.setQueryHint(getString(R.string.search_hint));

            // handle search event of the SearchView
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {

                    try {
                        // create an API service and set session token to request header
                        api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                        // create request object to check session timeout
                        Call<ResponseBody> call = api.getCheck();
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    // if exception occurs or inconsistent database in server
                                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                        Preferences.showDialog(activity, "Server Error", "Please try again !");
                                        return;
                                    }

                                    // if session is expired
                                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                        Preferences.goLogin(activity);
                                        return;
                                    }

                                    // call display function to search and display result in the increasing order of resident id by default
                                    display(new SearchParam(query.trim(), ((Location) mapSpinner.getSelectedItem()).getId(), "name", "asc"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();
                                Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
            case R.id.action_refresh_fragment_resident:
                activity.recreate();
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

        progressIndicator.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.INVISIBLE);

        try {
            // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get all floors' basic information
            Call<List<Location>> call = api.getFloors();
            call.enqueue(new Callback<List<Location>>() {
                @Override
                public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                    try {
                        // if exception occurs or inconsistent database in server
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.showDialog(activity, "Server Error", "Please try again !");
                            return;
                        }

                        // if session is expired
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(activity);
                            return;
                        }

                        // get list of Locations from response
                        locationList = response.body();

                        // add a special floor as a representative of all locations
                        locationList.add(0, new Location("all", "Show All Floors"));

                        // assign data contained in locationList to mapSpinner
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(activity, R.layout.item_spinner, locationList);
                        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                        mapSpinner.setAdapter(adapter);

                        // handle click event on each item in the mapSpinner
                        mapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    // create an API service and set session token to request header
                                    api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                    // create request object to check session timeout
                                    Call<ResponseBody> call = api.getCheck();
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            // if exception occurs or inconsistent database in server
                                            if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                return;
                                            }

                                            // if session is expired
                                            if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                Preferences.goLogin(activity);
                                                return;
                                            }

                                            // display sorted resident list as user's expectation basing on the order that user clicks the 3 headers: 'ID', 'Name', 'Location' in the resident list table
                                            display(new SearchParam(sv.getQuery().toString().trim(), ((Location) mapSpinner.getSelectedItem()).getId(), colName[sortIndex == -1 ? 0 : sortIndex], (sortIndex == -1 || !asc[sortIndex]) ? "asc" : "desc"));
                                            if (sortIndex == -1) {
                                                sortIndex = 0;
                                                asc[0] = false;
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            t.printStackTrace();
                                            Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                                        }
                                    });


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<List<Location>> call, Throwable t) {
                    t.printStackTrace();
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myView;
    }
}
