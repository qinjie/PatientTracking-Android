package com.example.intern.ptp.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.intern.library.PhotoView;
import com.example.intern.library.PhotoViewAttacher;
import com.example.intern.library.PhotoViewAttacher.OnMatrixChangedListener;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.Resident.ResidentActivity2;
import com.example.intern.ptp.views.widgets.LocatorMapPhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapFragment extends Activity implements PhotoViewAttacher.OnViewTapListener {

    @BindView(R.id.iv_photo)
    LocatorMapPhotoView mImageView;

    @BindView(R.id.map_layout)
    RelativeLayout layout;

    public static int radius = 4;
    public static int delta = 1;
    private List<Resident> residentList;
    private PhotoViewAttacher mAttacher;
    public static Bitmap mBitmap, sBitMap;
    private RectF rec;
    private String floorId;
    private Activity activity = this;

    /**
     * delegate handling-event task to appropriate view among PhotoView and TextViews on it
     */
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        // if more than 1 pointer is detected then let the PhotoView handle the touch event
        if (ev.getPointerCount() > 1) {
            return mImageView.dispatchTouchEvent(ev);
        }
        // dispatch the event normally
        return super.dispatchTouchEvent(ev);

    }

    /**
     * create BroadcastReceiver to receive broadcast data from MapService
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            try {
                // get result with Preferences.map_resultTag from MapService's broadcast intent
                String result = intent.getStringExtra(Preferences.map_resultTag);

                // if exception occurs or inconsistent database in server
                if (result.equalsIgnoreCase("failed")) {
                    Preferences.kill(activity, ":mapservice");
                    Preferences.showDialog(context, "Server Error", "Please try again!");
                    return;
                }

                // if connection is failed
                if (result.equalsIgnoreCase("connection_failure")) {
                    Preferences.kill(activity, ":mapservice");
                    Preferences.showDialog(activity, "Connection Failure", "Please check your network and try again!");
                    return;
                }

                // if session is expired
                if (!result.equalsIgnoreCase("isNotExpired")) {
                    Preferences.kill(activity, ":mapservice");
                    Preferences.goLogin(context);
                    return;
                }


                // if successfully receive map points from server
                residentList = intent.getParcelableArrayListExtra(Preferences.map_pointsTag);
                mImageView.setResidents(residentList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_map);
            ButterKnife.bind(this);

            // get floor id in the intent received from MapListFragment
            floorId = getIntent().getStringExtra(Preferences.floor_idTag);

            // set title related to the floor for the activity
            activity.setTitle("Map - " + getIntent().getStringExtra(Preferences.floor_labelTag));

            // get url for loading the map of the floor in the intent received from MapListFragment
            String url = Preferences.imageRoot + getIntent().getStringExtra(Preferences.floorFileParthTag);

            // use Picasso library to load the map
            Picasso.with(this)
                    .load(url)
                    .priority(Picasso.Priority.HIGH)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.NO_STORE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            try {
                                // store the original map to sBitMap
                                sBitMap = bitmap;

                                // assign the original map to mBitMap
                                mBitmap = bitmap;

                                // to be able to modify the map's pixels
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

                                // make the PhotoView reset when re assign bitmap
                                mImageView.setResetable(true);

                                // set bitmap to the PhotoView
                                mImageView.setImageBitmap(mBitmap);


                                // to be able to get coordinates of the rectangular map's 4 edges and handle zooming event
                                mAttacher = new PhotoViewAttacher(mImageView);
                                mAttacher.setOnMatrixChangeListener(mImageView);
                                mAttacher.setOnViewTapListener(MapFragment.this);


                                // create a new intent related to MapService
                                Intent serviceIntent = new Intent(activity, MapService.class);

                                // put floor id as an extra in the above created intent
                                serviceIntent.putExtra(Preferences.floor_idTag, floorId);

                                // register a broadcast receiver with the tag equals "Preferences.map_broadcastTag + floorId"
                                activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.map_broadcastTag + floorId));

                                // start a new MapService with the intent
                                activity.startService(serviceIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Preferences.dismissLoading();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            try {
                                // assign a null image to mBitMap when failed loading the map
                                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.null_image);

                                // to be able to modify the map's pixels
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

                                // set bitmap to the PhotoView
                                mImageView.setResetable(true);

                                // set bitmap to the PhotoView
                                mImageView.setImageBitmap(mBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Preferences.dismissLoading();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            try {
                                // assign a loading image to mBitMap when the map is being loaded
                                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_image);

                                // to be able to modify the map's pixels
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

                                // set bitmap to the PhotoView
                                mImageView.setResetable(true);

                                // set bitmap to the PhotoView
                                mImageView.setImageBitmap(mBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
            getMenuInflater().inflate(R.menu.menu_activity_map, menu);
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
    public void onDestroy() {
        super.onDestroy();
        try {
            // unregister the broadcast receiver when the activity is destroyed
            activity.unregisterReceiver(mMessageReceiver);

            // kill MapService process
            Preferences.kill(activity, ":mapservice");

            // clean up the map
            if (mAttacher != null) mAttacher.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
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
                    Preferences.dismissLoading();
                    activity.finish();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onBackPressed() {
        Preferences.dismissLoading();
        activity.finish();
    }


    @Override
    public void onViewTap(View view, float x, float y) {
        PointF touchPoint = new PointF(x,y);

        Resident resident = mImageView.getTouchedResident(touchPoint);

        if(resident != null) {
            // create a new intent related to ResidentActivity
            Intent intent = new Intent(activity, ResidentActivity.class);

            // put id of the resident as an extra in the above created intent
            intent.putExtra(Preferences.resident_idTag, resident.getId());

            // start a new ResidentActivity with the intent
            activity.startActivity(intent);
        }
    }
}
