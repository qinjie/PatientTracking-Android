package com.example.intern.ptp.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
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
    private Intent serviceIntent;

    private String floorId, url;
    private Activity activity = this;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return mImageView.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            try {
                String result = intent.getStringExtra(Preferences.map_resultTag);
                if (result.equalsIgnoreCase("failed")) {
                    Preferences.kill(activity, ":mapservice");
                    Preferences.showDialog(context, "Server Error", "Please try again!");
                    return;
                }
                if (!result.equalsIgnoreCase("isNotExpired")) {
                    Preferences.kill(activity, ":mapservice");
                    Preferences.goLogin(context);
                    return;
                }
                residentList = intent.getParcelableArrayListExtra(Preferences.map_pointsTag);
                drawMapPoints();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void drawMapPoints() {
        try {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View view = layout.getChildAt(i);
                if (view instanceof TextView) {
                    layout.removeView(view);
                    i--;
                }
            }

            mBitmap = sBitMap;
            mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);

            if (residentList != null) {
                for (final Resident resident : residentList) {
                    int pixelx = Integer.parseInt(resident.getPixelx());
                    int pixely = Integer.parseInt(resident.getPixely());
                    int color = Integer.parseInt(resident.getColor());

                    for (int i = pixelx - radius; i <= pixelx + radius; i += delta)
                        for (int k = pixely - radius; k <= pixely + radius; k += delta)
                            if (i >= 0 && i < mBitmap.getWidth() && k >= 0 && k < mBitmap.getHeight() && (i - pixelx) * (i - pixelx) + (k - pixely) * (k - pixely) <= radius * radius)
                                mBitmap.setPixel(i, k, color);

                    TextView textView = new TextView(activity);
                    textView.setText(resident.getFirstname());
                    textView.setTextColor(Color.RED);
                    textView.setBackgroundColor(Color.LTGRAY);
                    textView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));


                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(activity, ResidentActivity.class);
                                intent.putExtra(Preferences.resident_idTag, resident.getId());
                                activity.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    rec = mAttacher.getDisplayRect();
                    engage(textView, pixelx, pixely);
                    layout.addView(textView);
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
            Preferences.showLoading(activity);
            setContentView(R.layout.activity_map);
            layout = (RelativeLayout) findViewById(R.id.map_layout);
            mImageView = (PhotoView) findViewById(R.id.iv_photo);

            floorId = getIntent().getStringExtra(Preferences.floor_idTag);
            url = Preferences.imageRoot + getIntent().getStringExtra(Preferences.floorFileParthTag);

            Picasso.with(this)
                    .load(url)
                    .priority(Picasso.Priority.HIGH)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.NO_STORE)
//                    .memoryPolicy(MemoryPolicy.NO_STORE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            try {
                                sBitMap = bitmap;

                                mBitmap = bitmap;
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
                                mImageView.setResetable(true);
                                mImageView.setImageBitmap(bitmap);

                                mAttacher = new PhotoViewAttacher(mImageView);
                                mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());

                                serviceIntent = new Intent(activity, MapService.class);
                                floorId = getIntent().getStringExtra(Preferences.floor_idTag);
                                serviceIntent.putExtra(Preferences.floor_idTag, floorId);
                                activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.map_broadcastTag + floorId));
                                activity.startService(serviceIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Preferences.dismissLoading();
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            try {
                                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.null_image);
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
                                mImageView.setResetable(true);
                                mImageView.setImageBitmap(mBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Preferences.dismissLoading();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            try {
                                mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_image);
                                mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
                                mImageView.setResetable(true);
                                mImageView.setImageBitmap(mBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Preferences.dismissLoading();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void onResume() {
//        super.onResume();
//        try {
////            Preferences.kill(activity, ":mapservice");
////            activity.registerReceiver(mMessageReceiver, new IntentFilter(Preferences.map_broadcastTag + floorId));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    protected void onPause() {
//        super.onPause();
//        try {
////            activity.unregisterReceiver(mMessageReceiver);
////            Preferences.kill(activity, ":mapservice");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void engage(TextView tv, int pixelx, int pixely) {

        try {
            Rect bounds = new Rect();
            tv.getPaint().getTextBounds(tv.getText().toString(), 0, tv.getText().length(), bounds);
            int h = bounds.height();
            int w = bounds.width();

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
                actionBar.setDisplayShowTitleEnabled(true);
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
            activity.unregisterReceiver(mMessageReceiver);
            Preferences.kill(activity, ":mapservice");
            // Need to call clean-up

            if (mAttacher != null) mAttacher.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem zoomToggle = menu.findItem(R.id.menu_zoom_toggle);
//        assert null != zoomToggle;
//        zoomToggle.setTitle(mAttacher.canZoom() ? R.string.menu_zoom_disable : R.string.menu_zoom_enable);
//
        return super.onPrepareOptionsMenu(menu);
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
//                    Preferences.kill(activity, ":mapservice");
                    activity.finish();
                    return true;
                case R.id.action_refresh_map:
                    activity.recreate();
//                    Preferences.showLoading(activity);
//                    Picasso.with(this)
//                    .load(url)
//                        .priority(Picasso.Priority.HIGH)
//                        .networkPolicy(NetworkPolicy.NO_CACHE)
//                        .networkPolicy(NetworkPolicy.NO_STORE)
//                        .memoryPolicy(MemoryPolicy.NO_STORE)
//                        .memoryPolicy(MemoryPolicy.NO_CACHE)
//                        .into(new Target() {
//                            @Override
//                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                try {
//                                    sBitMap = bitmap;
//
//                                    mBitmap = bitmap;
//                                    mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
//                                    mImageView.setResetable(true);
//                                    mImageView.setImageBitmap(bitmap);
//
//                                    mAttacher = new PhotoViewAttacher(mImageView);
//                                    mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                Preferences.dismissLoading();
//                            }
//
//                            @Override
//                            public void onBitmapFailed(Drawable errorDrawable) {
//                                try {
//                                    mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.null_image);
//                                    mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
//                                    mImageView.setResetable(true);
//                                    mImageView.setImageBitmap(mBitmap);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                Preferences.dismissLoading();
//                            }
//
//                            @Override
//                            public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                try {
//                                    mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_image);
//                                    mBitmap = mBitmap.copy(mBitmap.getConfig() != null ? mBitmap.getConfig() : Bitmap.Config.ARGB_8888, true);
//                                    mImageView.setResetable(true);
//                                    mImageView.setImageBitmap(mBitmap);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                Preferences.dismissLoading();
//                            }
//                        });
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
        try {
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class MatrixChangeListener implements OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {

            drawMapPoints();
        }
    }
}
