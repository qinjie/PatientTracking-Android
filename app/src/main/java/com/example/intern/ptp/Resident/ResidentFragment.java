package com.example.intern.ptp.Resident;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResidentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResidentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResidentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SearchView sv;
    private TableLayout[] tls;
    private TextView[] headers;
    private Spinner spinner;
    private List<Location> locationList;
    private int sortIndex = -1;
    private String[] colName = {"id", "name", "floor_id"};
    private boolean asc[] = {true, true, true};
    private Activity context;
    private View myView;
    private ServerApi api;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public ResidentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResidentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResidentFragment newInstance(String param1, String param2) {
        ResidentFragment fragment = new ResidentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private int getPx(int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    public void display(SearchParam param) {
        try {
            api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            Call<List<Resident>> call = api.getSearch(param);
            call.enqueue(new Callback<List<Resident>>() {
                @Override
                public void onResponse(final Call<List<Resident>> call, Response<List<Resident>> response) {
                    try {
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(context, "Server Error", "Please try again !");
                            return;
                        }
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(context);
                            return;
                        }
                        for (TableLayout tl : tls) {
                            tl.removeAllViews();
                        }

                        List<Resident> res = response.body();

                        for (int i = 0; i < res.size(); i++) {
                            Resident r = res.get(i);
                            List<String> values = new ArrayList<>();
                            final String resId = r.getId();
                            final String flId = r.getFloorId();
                            values.add(resId);
                            values.add(r.getFirstname() + " " + r.getLastname());
                            values.add(r.getLabel());
                            TableRow[] trs = new TableRow[3];
                            TextView[] tvs = new TextView[3];
                            for (int k = 0; k < 3; k++) {
                                try {
                                    tvs[k] = new TextView(context);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                tvs[k].setText(values.get(k));
                                tvs[k].setTextColor(Color.BLUE);
                                tvs[k].setPadding(getPx(10), getPx(5), getPx(0), getPx(5));

                                trs[k] = new TableRow(context);
                                if (i % 2 == 0)
                                    trs[k].setBackgroundColor(Color.WHITE);
                                else
                                    trs[k].setBackgroundColor(Color.TRANSPARENT);
                                trs[k].setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT));

                                trs[k].addView(tvs[k]);
                                tls[k].addView(trs[k]);

                            }
                            tvs[1].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    try {
                                        api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                        Call<ResponseBody> call = api.getCheck();
                                        Preferences.showLoading(context);
                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try {
                                                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                        Preferences.dismissLoading();
                                                        Preferences.showDialog(context, "Server Error", "Please try again !");
                                                        return;
                                                    }
                                                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                        Preferences.goLogin(context);
                                                        return;
                                                    }
                                                    Intent intent = new Intent(context, ResidentActivity.class);
                                                    intent.putExtra(Preferences.resident_idTag, resId);
                                                    context.startActivity(intent);
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Preferences.dismissLoading();
                                                t.printStackTrace();
                                            }
                                        });
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                            tvs[2].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                        Call<ResponseBody> call = api.getCheck();
                                        Preferences.showLoading(context);
                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try {
                                                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                        Preferences.dismissLoading();
                                                        Preferences.showDialog(context, "Server Error", "Please try again !");
                                                        return;
                                                    }
                                                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                        Preferences.goLogin(context);
                                                        return;
                                                    }
                                                    Intent intent = new Intent(context, LocationActivity.class);
                                                    intent.putExtra(Preferences.floor_idTag, flId);
                                                    context.startActivity(intent);
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                Preferences.dismissLoading();
                                                t.printStackTrace();
                                            }
                                        });
                                    }catch (Exception e){
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
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity();
        Preferences.checkFcmTokenStatus(context);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
            if(actionBar != null){
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_resident));
            }

            sv = (SearchView) menu.findItem(R.id.search).getActionView();

            sv.setQueryHint(getString(R.string.search_hint));


            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(final String query) {

                    try {
                        api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                        Call<ResponseBody> call = api.getCheck();
                        Preferences.showLoading(context);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                        Preferences.dismissLoading();
                                        Preferences.showDialog(context, "Server Error", "Please try again !");
                                        return;
                                    }
                                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                        Preferences.goLogin(context);
                                        return;
                                    }
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
                            }
                        });
                    }catch (Exception e){
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
            case R.id.action_refresh_fragment_resident:
                context.recreate();
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

            api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
            Call<List<Location>> call = api.getFloors();
            Preferences.showLoading(context);
            call.enqueue(new Callback<List<Location>>() {
                @Override
                public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                    try {
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(context, "Server Error", "Please try again !");
                            return;
                        }
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(context);
                            return;
                        }
                        locationList = response.body();
                        locationList.add(0, new Location("all", "All floors"));
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(context, R.layout.item_spinner, locationList);
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                    Call<ResponseBody> call = api.getCheck();
                                    Preferences.showLoading(context);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                Preferences.dismissLoading();
                                                Preferences.showDialog(context, "Server Error", "Please try again !");
                                                return;
                                            }
                                            if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                Preferences.goLogin(context);
                                                return;
                                            }
                                            display(new SearchParam(sv.getQuery().toString().trim(), ((Location) spinner.getSelectedItem()).getId(), colName[sortIndex == -1 ? 0 : sortIndex], (sortIndex == -1 || !asc[sortIndex]) ? "asc" : "desc"));
                                            if(sortIndex == -1){
                                                sortIndex = 0;
                                                asc[0] = false;
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Preferences.dismissLoading();
                                            t.printStackTrace();
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
                                    api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                    Call<ResponseBody> call = api.getCheck();
                                    Preferences.showLoading(context);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            try {
                                                if (response.headers().get("result").equalsIgnoreCase("failed")) {
                                                    Preferences.dismissLoading();
                                                    Preferences.showDialog(context, "Server Error", "Please try again !");
                                                    return;
                                                }
                                                if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                    Preferences.goLogin(context);
                                                    return;
                                                }
                                                display(new SearchParam(sv.getQuery().toString().trim(), ((Location) spinner.getSelectedItem()).getId(), colName[k], asc[k] ? "asc" : "desc"));
                                                asc[k] = !asc[k];
                                                sortIndex = k;
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Preferences.dismissLoading();
                                            t.printStackTrace();
                                        }
                                    });
                                }
                            });
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


                @Override
                public void onFailure(Call<List<Location>> call, Throwable t) {
                    Preferences.dismissLoading();
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
