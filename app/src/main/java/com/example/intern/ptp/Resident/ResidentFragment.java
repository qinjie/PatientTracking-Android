package com.example.intern.ptp.Resident;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.intern.ptp.Location.Location;
import com.example.intern.ptp.Location.LocationActivity;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentFragment extends Fragment {

    private SearchView sv;
    private TableLayout[] tls;
    private TextView[] headers;
    private Spinner spinner;
    private List<Location> locationList;
    private int sortIndex = -1;
    private String[] colName = {"id", "name", "floor_id"};
    private boolean asc[] = {true, true, true};
    private Activity activity;
    private View myView;
    private ServerApi api;

    public ResidentFragment() {
        // Required empty public constructor
    }

    /**
     * convert dp to px
     */
    private int getPx(int dp) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

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
                            Preferences.dismissLoading();
                            Preferences.showDialog(activity, "Server Error", "Please try again !");
                            return;
                        }

                        // if session is expired
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(activity);
                            return;
                        }

                        // clear the resident list table
                        for (TableLayout tl : tls) {
                            tl.removeAllViews();
                        }

                        // get list of Residents from response
                        List<Resident> res = response.body();

                        for (int i = 0; i < res.size(); i++) {

                            // get current resident element in for loop
                            Resident r = res.get(i);

                            // values contains displayed text in a table row
                            List<String> values = new ArrayList<>();
                            final String resId = r.getId();
                            final String flId = r.getFloorId();
                            values.add(resId);
                            values.add(r.getFirstname() + " " + r.getLastname());
                            values.add(r.getLabel());

                            // create 3 table rows for the resident list table
                            TableRow[] trs = new TableRow[3];

                            // create 3 text views for the above 3 table rows
                            TextView[] tvs = new TextView[3];

                            for (int k = 0; k < 3; k++) {

                                // add text for each above text view
                                tvs[k] = new TextView(activity);
                                tvs[k].setText(values.get(k));
                                tvs[k].setTextColor(Color.BLUE);
                                tvs[k].setPadding(getPx(10), getPx(5), getPx(0), getPx(5));

                                // design each above table row
                                trs[k] = new TableRow(activity);
                                if (i % 2 == 0)
                                    trs[k].setBackgroundColor(Color.WHITE);
                                else
                                    trs[k].setBackgroundColor(Color.TRANSPARENT);
                                trs[k].setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT));

                                // add text view to table row
                                trs[k].addView(tvs[k]);

                                // add table row to table layout
                                tls[k].addView(trs[k]);

                            }

                            // set click event for each cell in the 'Name' column in resident list table
                            trs[1].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

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

                                                    // create a new intent related to ResidentActivity
                                                    Intent intent = new Intent(activity, ResidentActivity.class);

                                                    // put resident id of the resident as an extra in the above created intent
                                                    intent.putExtra(Preferences.resident_idTag, resId);

                                                    // start a new ResidentActivity with the intent
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
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            // set click event for each cell in the 'Location' column in resident list table
                            trs[2].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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

                                                    // create a new intent related to LocationActivity
                                                    Intent intent = new Intent(activity, LocationActivity.class);

                                                    // put floor id of the location as an extra in the above created intent
                                                    intent.putExtra(Preferences.floor_idTag, flId);

                                                    // start a new LocationActivity with the intent
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
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Preferences.dismissLoading();
                }

                @Override
                public void onFailure(Call<List<Resident>> call, Throwable t) {
                    Preferences.dismissLoading();
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

                                    // call display function to search and display result in the increasing order of resident id by default
                                    display(new SearchParam(query.trim(), ((Location) spinner.getSelectedItem()).getId(), "id", "asc"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Preferences.dismissLoading();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();
                                Preferences.dismissLoading();
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
        myView = inflater.inflate(R.layout.fragment_resident, container, false);

        try {
            spinner = (Spinner) myView.findViewById(R.id.spinner);

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

                        // get list of Locations from response
                        locationList = response.body();

                        // add a special floor as a representative of all locations
                        locationList.add(0, new Location("all", "All floors"));

                        // assign data contained in locationList to spinner
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(activity, R.layout.item_spinner, locationList);
                        spinner.setAdapter(adapter);

                        // handle click event on each item in the spinner
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    // create an API service and set session token to request header
                                    api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                    // create request object to check session timeout
                                    Call<ResponseBody> call = api.getCheck();
                                    Preferences.showLoading(activity);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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

                                            // display sorted resident list as user's expectation basing on the order that user clicks the 3 headers: 'ID', 'Name', 'Location' in the resident list table
                                            display(new SearchParam(sv.getQuery().toString().trim(), ((Location) spinner.getSelectedItem()).getId(), colName[sortIndex == -1 ? 0 : sortIndex], (sortIndex == -1 || !asc[sortIndex]) ? "asc" : "desc"));
                                            if (sortIndex == -1) {
                                                sortIndex = 0;
                                                asc[0] = false;
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
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        tls = new TableLayout[3];
                        tls[0] = (TableLayout) myView.findViewById(R.id.tableLayout1);
                        tls[1] = (TableLayout) myView.findViewById(R.id.tableLayout2);
                        tls[2] = (TableLayout) myView.findViewById(R.id.tableLayout3);

                        headers = new TextView[3];
                        headers[0] = (TextView) myView.findViewById(R.id.header1);
                        headers[1] = (TextView) myView.findViewById(R.id.header2);
                        headers[2] = (TextView) myView.findViewById(R.id.header3);
                        for (int i = 0; i < headers.length; i++) {
                            final int k = i;
                            headers[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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

                                                // display sorted resident list as user's expectation basing on the table header user clicks
                                                display(new SearchParam(sv.getQuery().toString().trim(), ((Location) spinner.getSelectedItem()).getId(), colName[k], asc[k] ? "asc" : "desc"));
                                                asc[k] = !asc[k];
                                                sortIndex = k;
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
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
