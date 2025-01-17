package com.example.intern.ptp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.intern.ptp.fragments.AlertHistoryFragment;
import com.example.intern.ptp.fragments.MapFragment;
import com.example.intern.ptp.fragments.NextOfKinFragment;
import com.example.intern.ptp.network.client.AlertClient;
import com.example.intern.ptp.network.client.ResidentClient;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.DemoUtil;
import com.example.intern.ptp.utils.FontManager;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.utils.UserManager;
import com.example.intern.ptp.utils.bus.response.NotificationMessage;
import com.example.intern.ptp.utils.bus.response.ServerError;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class ResidentActivity extends BaseActivity implements MapFragment.OnResidentTouchListener {

    private Resident resident;
    private Alert alert;
    private List<Fragment> fragments = new ArrayList<>();

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

    @BindView(R.id.resident_take_care_button)
    Button alertTakeCareButton;

    @BindView(R.id.resident_profile_picture)
    ImageView profilePicture;

    @BindView(R.id.resident_firstname)
    TextView firstName;

    @BindView(R.id.resident_lastname)
    TextView lastName;

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

        toggleGroup.setOnCheckedChangeListener(toggleListener);

        String residentId = this.getIntent().getStringExtra(Preferences.RESIDENT_ID);

        refreshView(residentId);
    }

    private void refreshView(String residentId) {
        showProgress(resident == null);

        ResidentClient service = ResidentClient.getClient();
        service.getResident(this, residentId);
    }

    private void showAlert(Alert alert) {
        this.alert = alert;

        if (alert == null || !alert.isOngoing()) {
            alertLayout.setVisibility(View.GONE);
            return;
        }

        alertLayout.setVisibility(View.VISIBLE);
        alertTakeCareButton.setVisibility(View.VISIBLE);
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
    public void takeCare(View view) {
        view.setVisibility(View.INVISIBLE);

        AlertClient service = AlertClient.getClient();
        service.postTakeCare(this, alert, UserManager.getName(this));
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_RESIDENT)) {
            onResidentRefresh((Resident) event.getMessage());
        } else if (event.getType().equals(ServerResponse.POST_TAKE_CARE)) {
            onTakeCare((Alert) event.getMessage());
        }
    }

    @Subscribe
    public void onServerError(ServerError serverError) {
        if (serverError.getType().equals(ServerError.ERROR_UNKNOWN)) {

            if (resident == null) {
                showError(getResources().getString(R.string.error_unknown_server_error));
            } else {
                Toast.makeText(this, R.string.error_unknown_server_error, Toast.LENGTH_SHORT).show();
                showContent();
            }
        }
    }

    public void onTakeCare(Alert alert) {

        if (!alert.isOngoing()) {
            alertLayout.animate()
                    .translationY(0)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            alertLayout.setVisibility(View.GONE);
                            alertLayout.setAlpha(1.0f);
                        }
                    });

            ResidentClient service = ResidentClient.getClient();
            service.getResident(this, resident.getId());
        } else {
            showAlert(alert);
        }
    }

    public void onResidentRefresh(Resident resident) {
        this.resident = resident;

        // TODO: REMOVE THIS. IS JUST FOR OLD API
        Drawable image = DemoUtil.getResidentProfileDrawable(this, resident.getId());
        profilePicture.setImageDrawable(image);
        // TODO END

        firstName.setText(resident.getFirstname());
        lastName.setText(resident.getLastname());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String birthdayText = resident.getBirthday();

        try {
            Date birthday = sdf.parse(resident.getBirthday());
            sdf.applyPattern("MMM dd, yyyy");
            birthdayText = sdf.format(birthday);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        birthday.setText(birthdayText);
        remark.setText(resident.getRemark());
        showAlert(resident.getOngoingAlert());

        if (fragments.size() == 0) {
            showMap();
        } else {
            for (Fragment frag : fragments) {
                if (frag instanceof AlertHistoryFragment) {
                    ((AlertHistoryFragment) frag).refresh(resident.getAlerts());
                } else if (frag instanceof NextOfKinFragment) {
                    ((NextOfKinFragment) frag).refresh(resident.getNextofkin());
                }
            }
        }

        showContent();
    }

    @Subscribe
    public void onNotificationResponse(NotificationMessage event) {
        if (event.getType().equals(NotificationMessage.MESSAGE_RECEIVED)) {
            Alert alert = (Alert) event.getMessage();

            if (resident != null && resident.getId().equals(alert.getResidentId())) {
                refreshView(resident.getId());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        try {
            getMenuInflater().inflate(R.menu.menu_activity_resident, menu);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                // search predefined title for action bar
                actionBar.setDisplayShowTitleEnabled(true);

                // search go-back-home arrow at the left most of the action bar
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
                case android.R.id.home: {
                    this.finish();
                    return true;
                }
                case R.id.action_refresh: {
                    String residentId = resident != null ? resident.getId() : null;
                    refreshView(residentId);
                    return true;
                }
                default: {
                    return super.onOptionsItemSelected(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onResidentTouched(Resident resident) {
        refreshView(resident.getId());
    }
}
