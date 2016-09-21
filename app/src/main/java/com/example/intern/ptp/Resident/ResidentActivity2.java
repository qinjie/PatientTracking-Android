package com.example.intern.ptp.Resident;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.ServerApi;
import com.example.intern.ptp.network.ServiceGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentActivity2 extends Activity {

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
        setContentView(R.layout.activity_resident2);
        ButterKnife.bind(this);

        toggleGroup.setOnCheckedChangeListener(toggleListener);

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
                        Preferences.showDialog(ResidentActivity2.this, "Server Error", "Please try again !");
                        return;
                    }

                    // if session is expired
                    if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                        Preferences.goLogin(ResidentActivity2.this);
                        return;
                    }

                    // get response data from server
                    Resident resident = response.body();

                    // display resident's information
                    firstName.setText(resident.getFirstname());
                    lastName.setText(resident.getLastname());
                    nric.setText(resident.getNric());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date d = sdf.parse(resident.getBirthday());
                    sdf.applyPattern("MMM dd, yyyy");
                    birthday.setText(sdf.format(d));
                    remark.setText(resident.getRemark());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();
                Preferences.showDialog(ResidentActivity2.this, "Connection Failure", "Please check your network and try again!");
            }
        });
    }

    public void onToggle(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
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
}
