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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class MapActivity extends Activity {

    public static Bitmap mBitmap, sBitMap;
    public static PhotoView mImageView;
    public static int radius = 4;
    public static int delta = 1;
    private List<Resident> residentList;
    private PhotoViewAttacher mAttacher;
    private RelativeLayout layout;
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

                // display received map points on the map
                drawMapPoints();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * display position of all elements in the residentList on the map by text views and modifying color of the map's pixels
     */
    private void drawMapPoints() {
        try {
            // remove old text views in the layout
            for (int i = 0; i < layout.getChildCount(); i++) {
                View view = layout.getChildAt(i);
                if (view instanceof TextView) {
                    layout.removeView(view);
                    i--;
                }
            }

            // get the original map
            mBitmap = sBitMap;

            // to be able to modify the map's pixels
            mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

            if (residentList != null) {
                for (final Resident resident : residentList) {
                    // get pixel coordinates of color for each resident or user if id = -1
                    int pixelx = Integer.parseInt(resident.getPixelx());
                    int pixely = Integer.parseInt(resident.getPixely());
                    int color = Integer.parseInt(resident.getColor());

                    if (resident.getId().equalsIgnoreCase("-1")) {

                        // draw pixels for user's position
                        for (int i = pixelx - 2 * radius; i <= pixelx + 2 * radius; i += delta)
                            for (int k = pixely - 2 * radius; k <= pixely + 2 * radius; k += delta) {
                                int val = (i - pixelx) * (i - pixelx) + (k - pixely) * (k - pixely);
                                if (i >= 0 && i < mBitmap.getWidth() && k >= 0 && k < mBitmap.getHeight() && (val <= 0.5 * radius * radius || (val >= 2 * radius * radius && val <= 4 * radius * radius)))
                                    mBitmap.setPixel(i, k, color);
                            }


                    } else {

                        // draw pixels for resident's position
                        for (int i = pixelx - radius; i <= pixelx + radius; i += delta)
                            for (int k = pixely - radius; k <= pixely + radius; k += delta)
                                if (i >= 0 && i < mBitmap.getWidth() && k >= 0 && k < mBitmap.getHeight() && (i - pixelx) * (i - pixelx) + (k - pixely) * (k - pixely) <= radius * radius)
                                    mBitmap.setPixel(i, k, color);

                        // create a text view to display firstname of each resident
                        TextView textView = new TextView(activity);
                        textView.setText(resident.getFirstname());
                        textView.setTextColor(color);
                        textView.setBackgroundColor(Color.TRANSPARENT);
                        textView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

                        // start ResidentActivity to show resident's detail when user licks on the text view
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    // create a new intent related to ResidentActivity
                                    Intent intent = new Intent(activity, ResidentActivity.class);

                                    // put id of the resident as an extra in the above created intent
                                    intent.putExtra(Preferences.resident_idTag, resident.getId());

                                    // start a new ResidentActivity with the intent
                                    activity.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // get coordinates of the rectangular map's 4 edges
                        rec = mAttacher.getDisplayRect();

                        // stick the text view with pixel coordinates in the map
                        engage(textView, pixelx, pixely);
                        layout.addView(textView);
                    }
                }
            }
            mImageView.setResetable(false);
            mImageView.setImageBitmap(mBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_map);
            layout = (RelativeLayout) findViewById(R.id.map_layout);
            mImageView = (PhotoView) findViewById(R.id.iv_photo);

            // get floor id in the intent received from MapFragment
            floorId = getIntent().getStringExtra(Preferences.floor_idTag);

            // set title related to the floor for the activity
            activity.setTitle("Map - " + getIntent().getStringExtra(Preferences.floor_labelTag));

            // get url for loading the map of the floor in the intent received from MapFragment
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
                                mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());

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

    /**
     * stick a text view to some pixel coordinates in the map
     */
    private void engage(TextView tv, int pixelx, int pixely) {

        try {
            // get the height and the width the test view
            Rect bounds = new Rect();
            tv.getPaint().getTextBounds(tv.getText().toString(), 0, tv.getText().length(), bounds);
            int h = bounds.height();
            int w = bounds.width();

            // set x and y coordinates of the text view in the Android screen's coordinate system that are corresponding to pixelx and pixely of the map
            tv.setX(rec.left + 1.0f * pixelx / mBitmap.getWidth() * (rec.right - rec.left) - 0.5f * w);
            tv.setY(rec.top + 1.0f * pixely / mBitmap.getHeight() * (rec.bottom - rec.top) - 2.0f * h);
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


    private class MatrixChangeListener implements OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {

            // draw text views again when user zooms in or zooms out
            drawMapPoints();
        }
    }
}
