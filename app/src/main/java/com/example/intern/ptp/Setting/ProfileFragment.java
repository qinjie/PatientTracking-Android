package com.example.intern.ptp.Setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
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

public class ProfileFragment extends Fragment {

    private Activity activity;

    @BindView(R.id.tvusername)
    TextView tvUsername;

    @BindView(R.id.tvemail)
    TextView tvEmail;

    public ProfileFragment() {
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
            inflater.inflate(R.menu.menu_fragment_profile, menu);
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                // set title for action bar and display it
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.title_fragment_profile));
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
                case R.id.action_refresh_fragment_profile:
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
        View myView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, myView);

        Preferences.showLoading(activity);

        tvUsername.setText(activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));

        Preferences.showLoading(activity);
        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to get email of the user from server
        Call<String> call = api.getEmail(activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
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
                    tvEmail.setText(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                Preferences.dismissLoading();
                Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
            }
        });

        return myView;
    }

    @OnClick(R.id.btlogout)
    public void performLogout() {
        Preferences.goLogin(activity);
    }
}
