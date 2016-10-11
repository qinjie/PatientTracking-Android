package com.example.intern.ptp.fragments;

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
import android.widget.ProgressBar;

import com.example.intern.library.PhotoViewAttacher;
import com.example.intern.ptp.services.MapPointsService;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.network.models.Resident;
import com.example.intern.ptp.utils.ProgressManager;
import com.example.intern.ptp.views.widgets.LocatorMapPhotoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapFragment extends Fragment implements PhotoViewAttacher.OnViewTapListener {

    @BindView(R.id.map_progress_indicator)
    ProgressBar progressIndicator;

    @BindView(R.id.map_photo)
    LocatorMapPhotoView photoView;

    private PhotoViewAttacher photoAttacher;

    private ProgressManager progressManager;

    private OnResidentTouchListener onResidentTouchListener;

    /**
     * create BroadcastReceiver to receive broadcast data from MapPointsService
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            try {
                // get result with Preferences.map_resultTag from MapPointsService's broadcast intent
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
                photoView.setResidents(residentList);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressManager = new ProgressManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_map, null);
        ButterKnife.bind(this, view);

        progressManager.initLoadingIndicator(photoView, progressIndicator);

        Bundle args = getArguments();

        if (getActivity() instanceof OnResidentTouchListener) {
            onResidentTouchListener = (OnResidentTouchListener) getActivity();
        }

        final String floorId = args.getString(Preferences.floor_idTag);
        final String url = args.getString(Preferences.floorFilePathTag);

        final Target mapLoadTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {
                    photoView.setResetable(true);
                    photoView.setImageBitmap(bitmap);

                    // to be able to get coordinates of the rectangular map's 4 edges and handle zooming event
                    photoAttacher = new PhotoViewAttacher(photoView);
                    photoAttacher.setOnMatrixChangeListener(photoView);
                    photoAttacher.setOnViewTapListener(MapFragment.this);

                    // create a new intent related to MapPointsService
                    Intent serviceIntent = new Intent(getActivity(), MapPointsService.class);
                    serviceIntent.putExtra(Preferences.floor_idTag, floorId);
                    getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Preferences.map_broadcastTag + floorId));
                    getActivity().startService(serviceIntent);

                    progressManager.stopProgress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                try {
                    progressManager.stopProgress();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                try {
                    // assign a null image to mBitMap when failed loading the map
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.null_image);

                    photoView.setImageBitmap(bitmap);
                    progressManager.indicateProgress(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        photoView.setTag(mapLoadTarget);

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

            // kill MapPointsService process
            Preferences.kill(getActivity(), ":mapservice");

            // clean up the map
            if (photoAttacher != null) photoAttacher.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        PointF touchPoint = new PointF(x, y);

        Resident resident = photoView.getTouchedResident(touchPoint);

        if (resident != null && !resident.isNurse()) {
            if (onResidentTouchListener != null) {
                onResidentTouchListener.onResidentTouched(resident);
            }
        }
    }

    public interface OnResidentTouchListener {
        void onResidentTouched(Resident resident);
    }
}
