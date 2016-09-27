package com.example.intern.ptp.Map;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.intern.library.PhotoViewAttacher;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.Resident.Resident;
import com.example.intern.ptp.Resident.ResidentActivity;
import com.example.intern.ptp.views.widgets.LocatorMapPhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapFragment extends Fragment implements PhotoViewAttacher.OnViewTapListener {

    @BindView(R.id.iv_photo)
    LocatorMapPhotoView mImageView;

    @BindView(R.id.map_layout)
    RelativeLayout layout;

    private PhotoViewAttacher mAttacher;

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
                    Preferences.kill(getActivity(), ":mapservice");
                    Preferences.showDialog(context, "Server Error", "Please try again!");
                    return;
                }

                // if connection is failed
                if (result.equalsIgnoreCase("connection_failure")) {
                    Preferences.kill(getActivity(), ":mapservice");
                    Preferences.showDialog(getActivity(), "Connection Failure", "Please check your network and try again!");
                    return;
                }

                // if session is expired
                if (!result.equalsIgnoreCase("isNotExpired")) {
                    Preferences.kill(getActivity(), ":mapservice");
                    Preferences.goLogin(context);
                    return;
                }

                // if successfully receive map points from server
                List<Resident> residentList = intent.getParcelableArrayListExtra(Preferences.map_pointsTag);
                mImageView.setResidents(residentList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_map, null);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();

        final String floorId = args.getString(Preferences.floor_idTag);
        final String url = args.getString(Preferences.floorFilePathTag);

        final Target mapLoadTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    // make the PhotoView reset when re assign bitmap
                    mImageView.setResetable(true);

                    // set bitmap to the PhotoView
                    mImageView.setImageBitmap(bitmap);

                    // to be able to get coordinates of the rectangular map's 4 edges and handle zooming event
                    mAttacher = new PhotoViewAttacher(mImageView);
                    mAttacher.setOnMatrixChangeListener(mImageView);
                    mAttacher.setOnViewTapListener(MapFragment.this);


                    // create a new intent related to MapService
                    Intent serviceIntent = new Intent(getActivity(), MapService.class);

                    // put floor id as an extra in the above created intent
                    serviceIntent.putExtra(Preferences.floor_idTag, floorId);

                    // register a broadcast receiver with the tag equals "Preferences.map_broadcastTag + floorId"
                    getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Preferences.map_broadcastTag + floorId));

                    // start a new MapService with the intent
                    getActivity().startService(serviceIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                try {
                    // assign a null image to mBitMap when failed loading the map
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.null_image);

                    mImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.dismissLoading();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                try {
                    // assign a loading image to mBitMap when the map is being loaded
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loading_image);

                    // set bitmap to the PhotoView
                    mImageView.setResetable(true);

                    // set bitmap to the PhotoView
                    mImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mImageView.setTag(mapLoadTarget);

        try {
            // use Picasso library to load the map
            Picasso.with(getActivity())
                    .load(url)
                    .priority(Picasso.Priority.HIGH)
                    .into(mapLoadTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            // unregister the broadcast receiver when the activity is destroyed
            getActivity().unregisterReceiver(mMessageReceiver);

            // kill MapService process
            Preferences.kill(getActivity(), ":mapservice");

            // clean up the map
            if (mAttacher != null) mAttacher.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        PointF touchPoint = new PointF(x, y);

        Resident resident = mImageView.getTouchedResident(touchPoint);

        if (resident != null) {
            Intent intent = new Intent(getActivity(), ResidentActivity.class);
            intent.putExtra(Preferences.resident_idTag, resident.getId());
            this.startActivityForResult(intent, 0);
        }
    }
}
