package com.example.intern.ptp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.AnimUtils;
import com.example.intern.ptp.utils.StateManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.views.widgets.ErrorView;
import com.squareup.otto.Bus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseFragment extends Fragment {

    StateManager stateManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bus bus = BusManager.getBus();
        bus.register(this);

        stateManager = new StateManager();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);
        stateManager.setErrorView((ErrorView) ButterKnife.findById(view, R.id.error_view));
        stateManager.setContentView(ButterKnife.findById(view, R.id.content_view));
        stateManager.setProgressView(ButterKnife.findById(view, R.id.progress_indicator));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        stateManager.setMenu(menu);
    }

    public void showProgress(boolean isInitialLoading) {
       stateManager.showProgress(isInitialLoading);
    }

    public void showError(String message) {
        stateManager.showError(message);
    }

    public void showContent() {
        stateManager.showContent();
    }

    public void showContentDelayed(int delay) {
        stateManager.showContentDelayed(delay);
    }
}
