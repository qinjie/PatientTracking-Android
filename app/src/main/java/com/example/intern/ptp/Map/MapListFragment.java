package com.example.intern.ptp.Map;

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

import com.example.intern.ptp.Location.Location;
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

public class MapListFragment extends Fragment {

    @BindView(R.id.mapListView)
    ListView mapListView;

    private MapListAdapter adapter;
    private Activity activity;
    private ServerApi api;

    public MapListFragment() {
        // Required empty public constructor
    }

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
            inflater.inflate(R.menu.menu_fragment_map, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and display it
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
                case R.id.action_refresh_fragment_map:
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
        View myView = inflater.inflate(R.layout.fragment_maplist, container, false);
        ButterKnife.bind(this, myView);

        adapter = new MapListAdapter(activity, new ArrayList<Location>());
        mapListView.setAdapter(adapter);

        try {
             // create an API service and set session token to request header
            api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get all floors' basic information
            Call<List<Location>> call = api.getFloors();
            Preferences.showLoading(activity);
            call.enqueue(new Callback<List<Location>>() {
                @Override
                public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
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

                        // assign data contained in mapList to mapListView
                        adapter.updateLocations(response.body());

                        // handle click event on each list view item
                        mapListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Preferences.showLoading(activity);

                                // create an API service and set session token to request header
                                api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                // create request object to check session timeout
                                Call<ResponseBody> call = api.getCheck();
                                final int pos = position;
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

                                            // create a new intent related to MapFragment
                                            Intent intent = new Intent(activity, MapActivity.class);
                                            Location location = (Location) adapter.getItem(pos);

                                            // put floor's file path of the location as an extra in the above created intent
                                            intent.putExtra(Preferences.floorFilePathTag, location.getFilePath());

                                            // put floor id of the location as an extra in the above created intent
                                            intent.putExtra(Preferences.floor_idTag, location.getId());

                                            // put floor's label of the location as an extra in the above created intent
                                            intent.putExtra(Preferences.floor_labelTag, location.getLabel());

                                            // start a new MapFragment with the intent
                                            activity.startActivity(intent);
                                        } catch (Exception e) {
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

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Preferences.dismissLoading();
                }

                @Override
                public void onFailure(Call<List<Location>> call, Throwable t) {
                    Preferences.dismissLoading();
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
