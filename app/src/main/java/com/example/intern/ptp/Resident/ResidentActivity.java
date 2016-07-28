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

import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Retrofit.ServiceGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentActivity extends Activity {

    private TextView tvID, tvFirstName, tvLastName, tvNric, tvGender, tvBirthday, tvContact, tvRemark;
    private TableLayout[] tableLayout;
    private LinearLayout layout;
    private String mID, mFirstName, mLastName, mNric, mGender, mBirthday, mContact, mRemark;
    private String nID, nFirstName, nLastName, nNric, nContact, nEmail, nRemark, nRelation;
    private ServerApi api;
    private Activity context = this;
    private int getPx(int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    private void doit(int i, String text, int k){
        try {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            param.weight = 1;
            if (text == null)
                text = "";
            TextView tv = new TextView(context);
            final String old = text;
            text = text.replace('\n', ' ');
            tv.setText(text);
            tv.setTextColor(Color.BLUE);
            tv.setPadding(getPx(10), getPx(5), getPx(0), getPx(5));
            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try {
                        new AlertDialog.Builder(context, R.style.MyDialogStyle)
                                .setMessage(old)
                                .show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            TableRow tr = new TableRow(context);
            if (k % 2 == 0)
                tr.setBackgroundColor(Color.WHITE);
            else
                tr.setBackgroundColor(Color.TRANSPARENT);
            tr.setLayoutParams(param);

            tr.addView(tv);
            tableLayout[i].addView(tr);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_resident);
            layout = (TableLayout) findViewById(R.id.layout);
            tableLayout = new TableLayout[8];
            tableLayout[0] = (TableLayout) findViewById(R.id.tbID);
            tableLayout[1] = (TableLayout) findViewById(R.id.tbFirstname);
            tableLayout[2] = (TableLayout) findViewById(R.id.tbLastname);
            tableLayout[3] = (TableLayout) findViewById(R.id.tbNRIC);
            tableLayout[4] = (TableLayout) findViewById(R.id.tbContact);
            tableLayout[5] = (TableLayout) findViewById(R.id.tbEmail);
            tableLayout[6] = (TableLayout) findViewById(R.id.tbRemark);
            tableLayout[7] = (TableLayout) findViewById(R.id.tbRelation);

            tvID = (TextView) findViewById(R.id.ID);
            tvFirstName = (TextView) findViewById(R.id.firstname);
            tvLastName = (TextView) findViewById(R.id.lastname);
            tvNric = (TextView) findViewById(R.id.nric);
            tvGender = (TextView) findViewById(R.id.gender);
            tvBirthday = (TextView) findViewById(R.id.birthday);
            tvContact = (TextView) findViewById(R.id.contact);
            tvRemark = (TextView) findViewById(R.id.remark);

            api = ServiceGenerator.createService(ServerApi.class, this.getApplicationContext().getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag).getString("token", ""));

            Call<Resident> call = api.getResident(this.getIntent().getStringExtra(Preferences.resident_idTag));
            call.enqueue(new Callback<Resident>() {
                @Override
                public void onResponse(Call<Resident> call, Response<Resident> response) {
                    try {
                        if (response.headers().get("result").equalsIgnoreCase("failed")) {
                            Preferences.dismissLoading();
                            Preferences.showDialog(context, "Server Error", "Please try again !");
                            return;
                        }
                        if (!response.headers().get("result").equalsIgnoreCase("isNotExpired")) {
                            Preferences.goLogin(context);
                            return;
                        }
                        Resident resident = response.body();
                        List<NextOfKin> nextOfKinList = resident.getNextofkin();
                        if (nextOfKinList.size() > 0) {
                            layout.setVisibility(View.VISIBLE);
                        }

                        mID = resident.getId();
                        mFirstName = resident.getFirstname();
                        mLastName = resident.getLastname();
                        mNric = resident.getNric();
                        mGender = resident.getGender();
                        mBirthday = resident.getBirthday();
                        mContact = resident.getContact();
                        mRemark = resident.getRemark();
                        tvID.setText(mID);
                        tvFirstName.setText(mFirstName);
                        tvLastName.setText(mLastName);
                        tvNric.setText(mNric);
                        tvGender.setText(mGender);
                        tvBirthday.setText(mBirthday);
                        tvContact.setText(mContact);
                        tvRemark.setText(mRemark);

                        for (int i = 0; i < nextOfKinList.size(); i++) {
                            NextOfKin nextOfKin = nextOfKinList.get(i);
                            nID = nextOfKin.getId();
                            doit(0, nID, i);
                            nFirstName = nextOfKin.getFirstName();
                            doit(1, nFirstName, i);
                            nLastName = nextOfKin.getLastName();
                            doit(2, nLastName, i);
                            nNric = nextOfKin.getNric();
                            doit(3, nNric, i);
                            nContact = nextOfKin.getContact();
                            doit(4, nContact, i);
                            nEmail = nextOfKin.getEmail();
                            doit(5, nEmail, i);
                            nRemark = nextOfKin.getRemark();
                            doit(6, nRemark, i);
                            nRelation = nextOfKin.getRelation();
                            doit(7, nRelation, i);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Preferences.dismissLoading();
                }

                @Override
                public void onFailure(Call<Resident> call, Throwable t) {
                    Preferences.dismissLoading();
                    t.printStackTrace();
                }
            });
        }catch (Exception e){
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
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }catch (Exception e){
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
                case R.id.action_refresh_resident:
                    context.recreate();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
