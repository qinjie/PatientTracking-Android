package com.example.intern.ptp.Resident;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentActivity extends Activity {

    @BindView(R.id.ID)
    TextView tvID;

    @BindView(R.id.firstname)
    TextView tvFirstName;

    @BindView(R.id.lastname)
    TextView tvLastName;

    @BindView(R.id.nric)
    TextView tvNric;

    @BindView(R.id.gender)
    TextView tvGender;

    @BindView(R.id.birthday)
    TextView tvBirthday;

    @BindView(R.id.contact)
    TextView tvContact;

    @BindView(R.id.remark)
    TextView tvRemark;

    @BindViews({R.id.tbID, R.id.tbFirstname, R.id.tbLastname, R.id.tbNRIC, R.id.tbContact, R.id.tbEmail, R.id.tbRemark, R.id.tbRelation})
    List<TableLayout> tableLayouts;

    @BindView(R.id.layout)
    LinearLayout layout;

    private String mID, mFirstName, mLastName, mNric, mGender, mBirthday, mContact, mRemark;
    private String nID, nFirstName, nLastName, nNric, nContact, nEmail, nRemark, nRelation;
    private Activity activity = this;

    private int getPx(int dp) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * add table cell with text param into i-th column of the next-of-kin table (0-based index)
     * use k to make the table more readable
     */
    private void addTableCell(int i, String text, int k) {
        try {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (text == null)
                text = "";
            TextView tv = new TextView(activity);
            final String old = text;

            // modify text in order to display the text in only 1 line in the table
            text = text.replace('\n', ' ');
            tv.setText(text);
            tv.setTextColor(Color.BLUE);
            tv.setPadding(getPx(10), getPx(5), getPx(0), getPx(5));

            // display full text as a dialog when user licks the table cell containing the modified text
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        new AlertDialog.Builder(activity, R.style.MyDialogStyle)
                                .setMessage(old)
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            TableRow tr = new TableRow(activity);

            // make the table more readable
            if (k % 2 == 0)
                tr.setBackgroundColor(Color.WHITE);
            else
                tr.setBackgroundColor(Color.TRANSPARENT);
            tr.setLayoutParams(param);

            // add text view to table row
            tr.addView(tv);

            // add table row to i-th table layout in tableLayouts array
            tableLayouts.get(i).addView(tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_resident);
            ButterKnife.bind(this);

            // create an API service and set session token to request header
            ServerApi api = ServiceGenerator.createService(ServerApi.class, activity.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            // create request object to get a resident's information
            Call<Resident> call = api.getResident(this.getIntent().getStringExtra(Preferences.resident_idTag));
            call.enqueue(new Callback<Resident>() {
                @Override
                public void onResponse(Call<Resident> call, Response<Resident> response) {
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

                        // get response data from server
                        Resident resident = response.body();

                        // get list of the resident's next-of-kins
                        List<NextOfKin> nextOfKinList = resident.getNextofkin();

                        // hide the next-of-kin table when there is no next-of-kin in the list
                        if (nextOfKinList.size() > 0) {
                            layout.setVisibility(View.VISIBLE);
                        } else {
                            layout.setVisibility(View.INVISIBLE);
                        }

                        // get resident's information
                        mID = resident.getId();
                        mFirstName = resident.getFirstname();
                        mLastName = resident.getLastname();
                        mNric = resident.getNric();
                        mGender = resident.getGender();
                        mBirthday = resident.getBirthday();
                        mContact = resident.getContact();
                        mRemark = resident.getRemark();

                        // display resident's information
                        tvID.setText(mID);
                        tvFirstName.setText(mFirstName);
                        tvLastName.setText(mLastName);
                        tvNric.setText(mNric);
                        tvGender.setText(mGender);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date d = sdf.parse(mBirthday);
                        sdf.applyPattern("MMM dd, yyyy");
                        tvBirthday.setText(sdf.format(d));
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
}
