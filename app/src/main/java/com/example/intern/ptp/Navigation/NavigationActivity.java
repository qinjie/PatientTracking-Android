package com.example.intern.ptp.Navigation;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import com.example.intern.ptp.Alert.AlertFragment;
import com.example.intern.ptp.Location.LocationFragment;
import com.example.intern.ptp.Map.MapFragment;
import com.example.intern.ptp.Nearest.NearestFragment;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.ResidentFragment;
import com.example.intern.ptp.Setting.ChangeFragment;
import com.example.intern.ptp.Setting.ProfileFragment;

public class NavigationActivity extends Activity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks
{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // check whether the device has successfully sent a registered FCM token to server, if not and the FCM token is available then send it
        Preferences.checkFcmTokenAndFirstLoginAlertStatus(this);
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_navigation);

            NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        Fragment fragment;

        switch (position) {
            case 0: {
                fragment = new ResidentFragment();
                break;
            }
            case 1: {
                fragment = new LocationFragment();
                break;
            }
            case 2: {
                fragment = new MapFragment();
                break;
            }
            case 3: {
                fragment = new AlertFragment();
                break;
            }
            case 4: {
                fragment = new NearestFragment();
                break;
            }
            case 5: {
                fragment = new ProfileFragment();
                break;
            }
            case 6: {
                fragment = new ChangeFragment();
                break;
            }
            default:
                fragment = new ResidentFragment();
                break;
        }
        // set up selected fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
