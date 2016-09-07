package com.example.intern.ptp.Setting;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeFragment extends Fragment {

    private Activity activity;
    private ServerApi api;

    @BindView(R.id.tvpassword_current)
    TextView tvPassWord_Current;

    @BindView(R.id.tvpassword_new)
    TextView tvPassword_New;

    @BindView(R.id.tvpassword_confirm)
    TextView tvPassword_Confirm;

    private String username, currentPassword, newPassword, confirmPassword;

    public ChangeFragment() {
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
            inflater.inflate(R.menu.menu_fragment_change, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and display it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_change));
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
                // reload fragment
                case R.id.action_refresh_fragment_change:
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
        confirmPassword = tvPassword_Confirm.getText().toString();

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
                                Preferences.showLoading(activity);

                                // create an API service and set session token to request header
                                api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                // create request object to requesting change passowrd
                                Call<String> call = api.setPassword(new ChangeInfo(username, currentPassword, newPassword, macAddress));
                                call.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {
                                            // get response information from server
                                            String result = response.body();

                                            // if exception occurs or inconsistent database in server
                                            if (response.headers().get("result").equalsIgnoreCase("failed") || result.equalsIgnoreCase("failed")) {
                                                Preferences.dismissLoading();
                                                Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                return;
                                            }

                                            // if session is expired
                                            if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                Preferences.goLogin(activity);
                                                return;
                                            }

                                            // if successfully changed password
                                            if (result.equalsIgnoreCase("success")) {
                                                Preferences.showDialog(activity, null, "Changed successfully !");

                                                // if the input current password is wrong
                                            } else if (result.equalsIgnoreCase("wrong")) {
                                                Preferences.showDialog(activity, null, "Wrong password !");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        Preferences.dismissLoading();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        t.printStackTrace();
                                        Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                                    }
                                });
                            } catch (Exception e) {
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

        } else {
            Preferences.showDialog(activity, null, "New and Confirm Password do not match. Please try again!");
        }            // get username from Shared Preferences
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
        confirmPassword = tvPassword_Confirm.getText().toString();

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
                                Preferences.showLoading(activity);

                                // create an API service and set session token to request header
                                api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

                                // create request object to requesting change passowrd
                                Call<String> call = api.setPassword(new ChangeInfo(username, currentPassword, newPassword, macAddress));
                                call.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        try {
                                            // get response information from server
                                            String result = response.body();

                                            // if exception occurs or inconsistent database in server
                                            if (response.headers().get("result").equalsIgnoreCase("failed") || result.equalsIgnoreCase("failed")) {
                                                Preferences.dismissLoading();
                                                Preferences.showDialog(activity, "Server Error", "Please try again !");
                                                return;
                                            }

                                            // if session is expired
                                            if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                                                Preferences.goLogin(activity);
                                                return;
                                            }

                                            // if successfully changed password
                                            if (result.equalsIgnoreCase("success")) {
                                                Preferences.showDialog(activity, null, "Changed successfully !");

                                                // if the input current password is wrong
                                            } else if (result.equalsIgnoreCase("wrong")) {
                                                Preferences.showDialog(activity, null, "Wrong password !");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        Preferences.dismissLoading();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        t.printStackTrace();
                                        Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                                    }
                                });
                            } catch (Exception e) {
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

        } else {
            Preferences.showDialog(activity, null, "New and Confirm Password do not match. Please try again!");
        }
    }
}
