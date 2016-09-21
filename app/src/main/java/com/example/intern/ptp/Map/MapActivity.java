package com.example.intern.ptp.Map;

import android.app.Activity;
import android.os.Bundle;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;

import butterknife.ButterKnife;


public class MapActivity extends Activity {

    private String floorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        // get floor id in the intent received from MapListFragment
        floorId = getIntent().getStringExtra(Preferences.floor_idTag);

        // set title related to the floor for the activity
        setTitle("Map - " + getIntent().getStringExtra(Preferences.floor_labelTag));

        // get url for loading the map of the floor in the intent received from MapListFragment
        String url = Preferences.imageRoot + getIntent().getStringExtra(Preferences.floorFileParthTag);
    }
}
