package com.example.intern.ptp;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.intern.ptp.fragments.MapFragment;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.Preferences;


public class MapActivity extends BaseActivity implements MapFragment.OnResidentTouchListener {

    Fragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String floorId = getIntent().getStringExtra(Preferences.floor_idTag);
        setTitle("Map - " + getIntent().getStringExtra(Preferences.floor_labelTag));
        String url = Preferences.imageRoot + getIntent().getStringExtra(Preferences.floorFilePathTag);

        FragmentManager manager = getFragmentManager();
        mapFragment = manager.findFragmentByTag("MapFragment");

        if (mapFragment == null) {
            mapFragment = new MapFragment();

            Bundle args = new Bundle();
            args.putString(Preferences.floor_idTag, floorId);
            args.putString(Preferences.floorFilePathTag, url);
            mapFragment.setArguments(args);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(android.R.id.content, mapFragment, "MapFragment").commit();
        }
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
                    this.finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {

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

    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onResidentTouched(Resident resident) {
        Intent intent = new Intent(this, ResidentActivity.class);
        intent.putExtra(Preferences.resident_idTag, resident.getId());
        this.startActivityForResult(intent, 0);
    }
}
