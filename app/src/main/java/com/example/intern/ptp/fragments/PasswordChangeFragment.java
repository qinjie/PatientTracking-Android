package com.example.intern.ptp.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.network.client.UserClient;
import com.example.intern.ptp.network.models.PasswordChangeInfo;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PasswordChangeFragment extends Fragment {

    private Activity activity;

    @BindView(R.id.tvpassword_current)
    TextView tvPassWord_Current;

    @BindView(R.id.tvpassword_new)
    TextView tvPassword_New;

    @BindView(R.id.tvpassword_confirm)
    TextView tvPassword_Confirm;

    private String username, currentPassword, newPassword;

    public PasswordChangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this.getActivity();

        Bus bus = BusManager.getBus();
        bus.register(this);

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
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and search it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_profile));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_change, container, false);
        ButterKnife.bind(this, myView);

        return myView;
    }

    @OnClick(R.id.btchange)
    public void changePassword() {
        // get username from Shared Preferences
        username = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

        // get input current password
        currentPassword = tvPassWord_Current.getText().toString();

        // empty current password is not allowed
        if (currentPassword.isEmpty()) {
            Preferences.showDialog(activity, null, "Please enter your current password !");
            return;
        }

        // get input new password
        newPassword = tvPassword_New.getText().toString();

        // empty new password is not allowed
        if (newPassword.isEmpty()) {
            Preferences.showDialog(activity, null, "Please enter your new password !");
            return;
        }

        // get input confirm password
        String confirmPassword = tvPassword_Confirm.getText().toString();

        // empty confirm password is not allowed
        if (confirmPassword.isEmpty()) {
            Preferences.showDialog(activity, null, "Please confirm your new password !");
            return;
        }

        // if new password and confirm password match
        if (newPassword.equals(confirmPassword)) {

            new AlertDialog.Builder(activity)
                    // make sure user want to change password
                    .setMessage("Are you sure ?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                // get MAC address of the device
                                WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
                                WifiInfo wInfo = wifiManager.getConnectionInfo();
                                final String macAddress = wInfo.getMacAddress();

                                UserClient userService = UserClient.getClient();
                                userService.changePassword(getActivity(), new PasswordChangeInfo(username, currentPassword, newPassword, macAddress));

                            } catch (Exception e) {

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

        } else {
            Preferences.showDialog(activity, null, "New and Confirm Password do not match. Please try again!");
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.POST_CHANGE_PASSWORD)) {
            String result = (String) event.getResponse();

            if (result.equalsIgnoreCase("success")) {
                Preferences.showDialog(activity, null, "Changed successfully !");

            } else if (result.equalsIgnoreCase("wrong")) {
                Preferences.showDialog(activity, null, "Wrong password !");
            }
        } else if (event.getType().equals(ServerResponse.ERROR_UNKNOWN)) {
            Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }
}
