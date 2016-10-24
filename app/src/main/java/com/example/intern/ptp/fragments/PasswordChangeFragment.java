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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intern.ptp.R;
import com.example.intern.ptp.network.client.UserClient;
import com.example.intern.ptp.network.models.PasswordChangeInfo;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.StateManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PasswordChangeFragment extends BaseFragment {

    private Activity activity;

    @BindView(R.id.tvpassword_current)
    TextView tvPassWord_Current;

    @BindView(R.id.tvpassword_new)
    TextView tvPassword_New;

    @BindView(R.id.tvpassword_confirm)
    TextView tvPassword_Confirm;

    private String username, currentPassword, newPassword;

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

        return inflater.inflate(R.layout.fragment_password_change, container, false);
    }

    @OnClick(R.id.btchange)
    public void changePassword() {
        // get username from Shared Preferences
        username = activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", "");

        // get input current password
        currentPassword = tvPassWord_Current.getText().toString();

        // empty current password is not allowed
        if (currentPassword.isEmpty()) {
            Toast.makeText(getActivity(), R.string.validation_current_password, Toast.LENGTH_SHORT).show();
            return;
        }

        // get input new password
        newPassword = tvPassword_New.getText().toString();

        // empty new password is not allowed
        if (newPassword.isEmpty()) {
            Toast.makeText(getActivity(), R.string.validation_new_password, Toast.LENGTH_SHORT).show();
            return;
        }

        // get input confirm password
        String confirmPassword = tvPassword_Confirm.getText().toString();

        // empty confirm password is not allowed
        if (confirmPassword.isEmpty()) {
            Toast.makeText(getActivity(), R.string.validation_confirm_password, Toast.LENGTH_SHORT).show();
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

                                showProgress(true);

                                UserClient userService = UserClient.getClient();
                                userService.changePassword(getActivity(), new PasswordChangeInfo(username, currentPassword, newPassword, macAddress));

                            } catch (Exception e) {

                                showContent();
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
            Toast.makeText(getActivity(), R.string.validation_match_password, Toast.LENGTH_SHORT).show();;
        }
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.POST_CHANGE_PASSWORD)) {
            String result = (String) event.getMessage();

            if (result.equalsIgnoreCase("success")) {
                Toast.makeText(getActivity(), R.string.success_change_password, Toast.LENGTH_SHORT).show();

            } else if (result.equalsIgnoreCase("wrong")) {
                Toast.makeText(getActivity(), R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
            }

            showContent();
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {
            showContent();
            Toast.makeText(getActivity(), R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
        }
    }
}
