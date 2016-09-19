package com.example.intern.ptp.Resident;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident2);
        ButterKnife.bind(this);

        // create an API service and set session token to request header
        ServerApi api = ServiceGenerator.createService(ServerApi.class, getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

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
                    birthday0..setText(sdf.format(d));
                    tvContact.setText(mContact);
                    tvRemark.setText(mRemark);

                    // display next-of-kin information in each table cell
                    for (int i = 0; i < nextOfKinList.size(); i++) {
                        NextOfKin nextOfKin = nextOfKinList.get(i);
                        nID = nextOfKin.getId();
                        addTableCell(0, nID, i);
                        nFirstName = nextOfKin.getFirstName();
                        addTableCell(1, nFirstName, i);
                        nLastName = nextOfKin.getLastName();
                        addTableCell(2, nLastName, i);
                        nNric = nextOfKin.getNric();
                        addTableCell(3, nNric, i);
                        nContact = nextOfKin.getContact();
                        addTableCell(4, nContact, i);
                        nEmail = nextOfKin.getEmail();
                        addTableCell(5, nEmail, i);
                        nRemark = nextOfKin.getRemark();
                        addTableCell(6, nRemark, i);
                        nRelation = nextOfKin.getRelation();
                        addTableCell(7, nRelation, i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onFailure(Call<Resident> call, Throwable t) {
                Preferences.dismissLoading();
                t.printStackTrace();
                Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
            }
        });
    } catch (Exception e) {
        e.printStackTrace();
    }

    }
}
