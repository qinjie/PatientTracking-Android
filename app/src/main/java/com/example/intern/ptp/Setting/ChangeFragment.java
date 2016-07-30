package com.example.intern.ptp.Setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.intern.ptp.Location.LocationFragment;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View myView;
    private Activity context;
    private ServerApi api;
    private TextView tvPassWord_Current, tvPassword_New, tvPassword_Confirm;
    private Button btChange;
    private String username, currentPassword, newPassword, confirmPassword;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ChangeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangeFragment newInstance(String param1, String param2) {
        ChangeFragment fragment = new ChangeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
            inflater.inflate(R.menu.menu_fragment_change, menu);
            ActionBar actionBar = context.getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_change));
            }
        }catch (Exception e){
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
                case R.id.action_refresh_fragment_change:
                    context.recreate();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_change, container, false);

        tvPassWord_Current = (TextView) myView.findViewById(R.id.tvpassword_current);
        tvPassword_New = (TextView) myView.findViewById(R.id.tvpassword_new);
        tvPassword_Confirm = (TextView) myView.findViewById(R.id.tvpassword_confirm);

        btChange = (Button) myView.findViewById(R.id.btchange);
        btChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");
                currentPassword = tvPassWord_Current.getText().toString();
                if(currentPassword.isEmpty()){
                    Preferences.showDialog(context, null, "Please enter your current password !");
                    return;
                }
                newPassword = tvPassword_New.getText().toString();
                if(newPassword.isEmpty()){
                    Preferences.showDialog(context, null, "Please enter your new password !");
                    return;
                }
                confirmPassword = tvPassword_Confirm.getText().toString();
                if(confirmPassword.isEmpty()){
                    Preferences.showDialog(context, null, "Please confirm your new password !");
                    return;
                }

                if(newPassword.equals(confirmPassword)){

                    new AlertDialog.Builder(context)
                            .setMessage("Are you sure ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                        WifiInfo wInfo = wifiManager.getConnectionInfo();
                                        final String macAddress = wInfo.getMacAddress();
                                        Preferences.showLoading(context);
                                        api = ServiceGenerator.createService(ServerApi.class, context.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));
                                        Call<String> call = api.setPassword(new ChangeInfo(username, currentPassword, newPassword, macAddress));
                                        call.enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                try {
                                                    String result = response.body();
                                                    if (response.headers().get("result").equalsIgnoreCase("failed") || result.equalsIgnoreCase("failed")) {
                                                        Preferences.dismissLoading();
                                                        Preferences.showDialog(context, "Server Error", "Please try again !");
                                                        return;
                                                    }
                                                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                        Preferences.goLogin(context);
                                                        return;
                                                    }
                                                    if (result.equalsIgnoreCase("success")) {
                                                        Preferences.showDialog(context, null, "Changed successfully !");
                                                    } else if (result.equalsIgnoreCase("wrong")) {
                                                        Preferences.showDialog(context, null, "Wrong password !");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                Preferences.dismissLoading();
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                t.printStackTrace();
                                                Preferences.showDialog(context, "Result", t.getMessage());
                                            }
                                        });
                                    }catch (Exception e){
                                        Preferences.dismissLoading();
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }else{
                    Preferences.showDialog(context, null, "New and Confirm Password do not match. Please try again!");
                }
            }
        });
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
