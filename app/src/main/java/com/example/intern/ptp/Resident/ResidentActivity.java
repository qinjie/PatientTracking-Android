package com.example.intern.ptp.Resident;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.intern.ptp.Alert.Alert;
import com.example.intern.ptp.Alert.AlertHistoryFragment;
import com.example.intern.ptp.Map.MapFragment;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.rest.AlertService;
import com.example.intern.ptp.network.rest.ResidentService;
import com.example.intern.ptp.utils.FontManager;
import com.example.intern.ptp.utils.UserManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.utils.bus.response.NotificationResponse;
import com.example.intern.ptp.utils.bus.response.ServerResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResidentActivity extends Activity {

    private Resident resident;
    private Alert alert;
    List<Fragment> fragments = new ArrayList<>();

    @BindView(R.id.maplist_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.resident_content)
    View contentView;

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
        ButterKnife.bind(this);

        Bus bus = BusManager.getBus();
        bus.register(this);

        toggleGroup.setOnCheckedChangeListener(toggleListener);
        refreshView();
    }

    private void refreshView() {
        progressIndicator.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.INVISIBLE);

        String residentId = this.getIntent().getStringExtra(Preferences.resident_idTag);

        ResidentService service = ResidentService.getService();
        service.getResident(this, residentId);
    }

    private void showAlert(Alert alert) {
        this.alert = alert;

        if (alert == null || !alert.isOngoing()) {
            alertLayout.setVisibility(View.GONE);
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
    public void takeCare(View view) {
        AlertService service = AlertService.getService();
        service.postTakeCare(this, alert.getId(), UserManager.getName(this));
    }

    @Subscribe
    public void onServerResponse(ServerResponse event) {
        if (event.getType().equals(ServerResponse.GET_RESIDENT)) {
            onResidentRefresh((Resident) event.getResponse());
        } else if (event.getType().equals(ServerResponse.POST_TAKE_CARE)) {
            onTakeCare((String) event.getResponse());
        }
    }

    public void onTakeCare(String success) {
        // if successfully sent take-care-action request to server
        if (success.equalsIgnoreCase("success") || !success.equalsIgnoreCase("failed")) {
            alert.setOk("1");
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

            ResidentService service = ResidentService.getService();
            service.getResident(this, resident.getId());
        }
    }

    public void onResidentRefresh(Resident resident) {
        this.resident = resident;

        // TODO: REMOVE THIS. IS JUST FOR 2016 SEPT DEMO
        String pictureName = "profile" + resident.getId();
        Drawable image = getDrawable(getResources().getIdentifier(pictureName, "drawable", getPackageName()));

        if (image == null) {
            image = getDrawable(getResources().getIdentifier("profile31", "drawable", getPackageName()));
        }

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

        progressIndicator.setVisibility(View.INVISIBLE);
        contentView.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onNotificationResponse(NotificationResponse event) {
        if (event.getType().equals(NotificationResponse.MESSAGE_RECEIVED)) {
            refreshView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
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
                    refreshView();
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
