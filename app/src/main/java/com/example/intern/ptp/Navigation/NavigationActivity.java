package com.example.intern.ptp.Navigation;

import android.app.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;

import com.example.intern.ptp.Alert.AlertFragment;
import com.example.intern.ptp.Location.LocationFragment;
import com.example.intern.ptp.Map.MapFragment;
import com.example.intern.ptp.Nearest.NearestFragment;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.ResidentFragment;
import com.example.intern.ptp.Retrofit.ServerApi;
import com.example.intern.ptp.Setting.ChangeFragment;
import com.example.intern.ptp.Setting.ProfileFragment;

public class NavigationActivity extends Activity
        implements
        ResidentFragment.OnFragmentInteractionListener,
        LocationFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        AlertFragment.OnFragmentInteractionListener,
        NearestFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        ChangeFragment.OnFragmentInteractionListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ServerApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences.checkFcmTokenStatus(this);
        super.onCreate(savedInstanceState);
//        if (savedInstanceState == null)
        {
            setContentView(R.layout.activity_navigation);

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));


            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
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
            case 1:{
                fragment = new LocationFragment();
                break;
            }
            case 2:{
                fragment = new MapFragment();
                break;
            }
            case 3:{
                fragment = new AlertFragment();
                break;
            }
            case 4:{
                fragment = new NearestFragment();
                break;
            }
            case 5:{
                fragment = new ProfileFragment();
                break;
            }
            case 6:{
                fragment = new ChangeFragment();
                break;
            }
            default:
                fragment = new ResidentFragment();
                break;
        }
            FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
//
//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//            case 4:
//                mTitle = getString(R.string.title_section4);
//                break;
//            case 5:
//                mTitle = getString(R.string.title_section5);
//                break;
//        }
//    }
//



    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            return inflater.inflate(R.layout.fragment_navigation, container, false);
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((NavigationActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }
//
}
