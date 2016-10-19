package com.example.intern.ptp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.intern.ptp.utils.AnimUtils;
import com.example.intern.ptp.utils.StateManager;
import com.example.intern.ptp.utils.bus.BusManager;
import com.example.intern.ptp.views.widgets.ErrorView;
import com.squareup.otto.Bus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseActivity extends Activity {

    StateManager stateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bus bus = BusManager.getBus();
        bus.register(this);

        stateManager = new StateManager();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        ButterKnife.bind(this);
        stateManager.setErrorView((ErrorView) ButterKnife.findById(this, R.id.error_view));
        stateManager.setContentView(ButterKnife.findById(this, R.id.content_view));
        stateManager.setProgressView(ButterKnife.findById(this, R.id.progress_indicator));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Bus bus = BusManager.getBus();
        bus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        stateManager.setMenu(menu);

        return super.onCreateOptionsMenu(menu);
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
