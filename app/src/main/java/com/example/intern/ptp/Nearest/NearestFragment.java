package com.example.intern.ptp.Nearest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NearestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NearestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Activity activity;
    private View myView;
    private String username;
    private TextView tvResident, tvDistance;
    private boolean red = true;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            try {
                String result = intent.getStringExtra(Preferences.nearest_resultTag);
                if(result.equalsIgnoreCase("failed")){
                    Preferences.kill(activity, ":nearestservice");
                    Preferences.showDialog(context, "Server Error", "Please try again!");
                    return;
                }
                if(!result.equalsIgnoreCase("isNotExpired")){
                    Preferences.goLogin(context);
                    return;
                }
                Resident resident = intent.getParcelableExtra(Preferences.nearest_residentTag);
                if (resident != null) {
                    tvResident.setText(resident.getFirstname() + " " + resident.getLastname());
                    tvDistance.setText(resident.getDistance());
                    if(red){
                        tvResident.setTextColor(Color.RED);
                        tvDistance.setTextColor(Color.RED);
                    }else{
                        tvResident.setTextColor(Color.BLUE);
                        tvDistance.setTextColor(Color.BLUE);
                    }
                    red = !red;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NearestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NearestFragment newInstance(String param1, String param2) {
        NearestFragment fragment = new NearestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NearestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();
        Preferences.checkFcmTokenStatus(activity);
        username = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");
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
            inflater.inflate(R.menu.menu_fragment_nearest, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_neasrest_resident));
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
        myView = inflater.inflate(R.layout.fragment_nearest, container, false);
        tvResident = (TextView) myView.findViewById(R.id.tvResident);
        tvDistance = (TextView) myView.findViewById(R.id.tvDistance);
        try {
            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.nearest_broadcastTag + username));
            activity.startService(new Intent(activity, NearestService.class));
        }catch (Exception e){
            e.printStackTrace();
        }
        return myView;
    }

    public void onResume() {
        super.onResume();
        try {
            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.nearest_broadcastTag + username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver(mMessageReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
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
    public void onDestroy() {
        Preferences.kill(activity, ":nearestservice");
        try {
            activity.unregisterReceiver(mMessageReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        try {
            activity.unregisterReceiver(mMessageReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        Preferences.kill(activity, ":nearestservice");
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
