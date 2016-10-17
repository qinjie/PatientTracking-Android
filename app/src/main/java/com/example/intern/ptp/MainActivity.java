package com.example.intern.ptp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import com.example.intern.ptp.R;
import com.example.intern.ptp.fragments.AlertListFragment;
import com.example.intern.ptp.fragments.MapListFragment;
import com.example.intern.ptp.fragments.NearestResidentFragment;
import com.example.intern.ptp.fragments.PasswordChangeFragment;
import com.example.intern.ptp.fragments.ResidentListFragment;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.views.navigation.NavigationDrawerFragment;

public class MainActivity extends Activity
        implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    boolean doubleBackToExitPressedOnce = false;

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
            case 2: {
                fragment = new AlertListFragment();
                break;
            }
            case 3: {
                fragment = new MapListFragment();
                break;
            }
            case 4: {
                fragment = new ResidentListFragment();
                break;
            }
            case 6: {
                fragment = new NearestResidentFragment();
                break;
            }
            case 7: {
                fragment = new PasswordChangeFragment();
                break;
            }
            case 8: {
                Preferences.goLogin(this);
                return;
            }
            default:
                fragment = new AlertListFragment();
                break;
        }
        // set up selected fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.backbutton_exit_warning, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
