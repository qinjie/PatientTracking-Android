package com.example.intern.ptp.Resident;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.Alert.AlertHistoryFragment;
import com.example.intern.ptp.Map.MapFragment;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;
import com.example.intern.ptp.utils.FontManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentActivity extends Activity {

    private Resident resident;
    private Alert alert;
    List<Fragment> fragments = new ArrayList<>();

    @BindView(R.id.resident_alert_layout)
    RelativeLayout alertLayout;

    @BindView(R.id.resident_location_icon)
    TextView alertLocationIcon;

    @BindView(R.id.resident_location)
    TextView alertLocation;

    @BindView(R.id.resident_time_icon)
    TextView alertTimeIcon;

    @BindView(R.id.resident_time)
    TextView alertTime;

    @BindView(R.id.resident_firstname)
    TextView firstName;

    @BindView(R.id.resident_lastname)
    TextView lastName;

    @BindView(R.id.resident_nric)
    TextView nric;

    @BindView(R.id.resident_birthday)
    TextView birthday;

    @BindView(R.id.resident_remark)
    TextView remark;

    @BindView(R.id.resident_view_toggle)
    RadioGroup toggleGroup;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident);
        ButterKnife.bind(this);

        toggleGroup.setOnCheckedChangeListener(toggleListener);

        initAlert();

        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        Preferences.showLoading(this);

        // create request object to get a resident's information
        Call<Resident> call = api.getResident(this.getIntent().getStringExtra(Preferences.resident_idTag));
        call.enqueue(new Callback<Resident>() {
            @Override
            public void onResponse(Call<Resident> call, Response<Resident> response) {
                try {
                    // if exception occurs or inconsistent database in server
                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                        Preferences.dismissLoading();
                        Preferences.showDialog(ResidentActivity.this, "Server Error", "Please try again !");
                        return;
                    }

                    // if session is expired
                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                        Preferences.goLogin(ResidentActivity.this);
                        return;
                    }

                    // get response data from server
                    resident = response.body();

                    // display resident's information
                    firstName.setText(resident.getFirstname());
                    lastName.setText(resident.getLastname());
                    nric.setText(resident.getNric());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date d = sdf.parse(resident.getBirthday());
                    sdf.applyPattern("MMM dd, yyyy");
                    birthday.setText(sdf.format(d));
                    remark.setText(resident.getRemark());

                    showMap();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();
                Preferences.showDialog(ResidentActivity.this, "Connection Failure", "Please check your network and try again!");
            }
        });
    }

    private void initAlert() {
        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            return;
        }

        alert = (Alert) bundle.get(Preferences.BUNDLE_KEY_ALERT);

        if (alert == null) {
            return;
        } else if (!alert.getOk().equalsIgnoreCase("0")) {
            return;
        }

        alertLayout.setVisibility(View.VISIBLE);
        alertLocationIcon.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        alertTimeIcon.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        alertLocation.setText(alert.getLastPositionLabel());

        CharSequence timeSinceAlarm;

        try {
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdAt = dateParser.parse(alert.getCreatedAt());
            timeSinceAlarm = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        } catch (ParseException ex) {
            timeSinceAlarm = alert.getCreatedAt();
        }

        alertTime.setText(timeSinceAlarm);
    }

    private void showMap() {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("MapFragment");

        if (fragment == null) {
            fragment = new MapFragment();
            String url = Preferences.imageRoot + resident.getFilePath();
            Bundle args = new Bundle();
            args.putString(Preferences.floor_idTag, resident.getFloorId());
            args.putString(Preferences.floorFilePathTag, url);
            fragment.setArguments(args);
        }

        showFragment(fragment, "MapFragment");
    }

    private void showAlertHistory() {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("AlertHistoryFragment");

        if (fragment == null) {
            fragment = new AlertHistoryFragment();
            Bundle args = new Bundle();
            args.putParcelableArrayList(Preferences.BUNDLE_KEY_ALERT, new ArrayList<Parcelable>(resident.getAlerts()));
            fragment.setArguments(args);
        }

        showFragment(fragment, "AlertHistoryFragment");
    }

    private void showNextOfKin() {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag("NextOfKinFragment");

        if (fragment == null) {
            fragment = new NextOfKinFragment();
            Bundle args = new Bundle();
            args.putParcelableArrayList(Preferences.BUNDLE_KEY_NEXT_OF_KINS, new ArrayList<Parcelable>(resident.getNextofkin()));
            fragment.setArguments(args);
        }

        showFragment(fragment, "NextOfKinFragment");
    }

    private void showFragment(Fragment fragment, String fragmentTag) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ft.replace(R.id.resident_fragment_container, fragment, fragmentTag);
        ft.commit();

        fragments.add(fragment);
    }

    public void onToggle(View view) {
        ((RadioGroup) view.getParent()).check(view.getId());

        if (view.getId() == R.id.resident_map_button) {
            showMap();
        } else if (view.getId() == R.id.resident_alert_history_button) {
            showAlertHistory();
        } else if (view.getId() == R.id.resident_next_of_kin_button) {
            showNextOfKin();
        }
    }

    static final RadioGroup.OnCheckedChangeListener toggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
            }
        }
    };

    @OnClick(R.id.resident_take_care_button)
    public void onTakeCare(View view) {
        ServerApi api = ServiceGenerator.createService(ServerApi.class, this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

        // create request object to send a take-care-action request to server
        Call<String> call = api.setTakecare(alert.getId(), this.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("username", ""));
        Preferences.showLoading(this);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    // if exception occurs or inconsistent database in server
                    if (response.headers().get("result").equalsIgnoreCase("failed")) {
                        Preferences.dismissLoading();
                        Preferences.showDialog(ResidentActivity.this, "Server Error", "Please try again !");
                        return;
                    }

                    // if session is expired
                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                        Preferences.goLogin(ResidentActivity.this);
                        return;
                    }

                    // get response from server
                    String res = response.body();

                    // if successfully sent take-care-action request to server
                    if (res.equalsIgnoreCase("success") || !res.equalsIgnoreCase("failed")) {
                        alert.setOk("1");
                        alertLayout.animate()
                                .translationY(0)
                                .alpha(0.0f)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        alertLayout.setVisibility(View.GONE);
                                    }
                                });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();
                Preferences.showDialog(ResidentActivity.this, "Connection Failure", "Please check your network and try again!");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.menu_activity_resident, menu);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                // display predefined title for action bar
                actionBar.setDisplayShowTitleEnabled(true);

                // display go-back-home arrow at the left most of the action bar
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            int id = item.getItemId();

            switch (id) {
                case android.R.id.home:
                    // app icon in action bar clicked; goto parent activity.
                    this.finish();
                    return true;

                // reload activity
                case R.id.action_refresh_resident:
                    recreate();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
