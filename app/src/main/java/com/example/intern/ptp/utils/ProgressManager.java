package com.example.intern.ptp.utils;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.intern.ptp.R;

public class ProgressManager {

    private Menu optionMenu;
    private int refreshViewId;

    private View contentView;
    private View indicatorView;

    public void initRefreshingIndicator(Menu optionMenu, int refreshViewId) {
        this.optionMenu = optionMenu;
        this.refreshViewId = refreshViewId;
    }

    public void initLoadingIndicator(View contentView, View indicatorView) {
        this.contentView = contentView;
        this.indicatorView = indicatorView;
    }

    public void indicateProgress(boolean isInitialLoading) {
        if(isInitialLoading) {
            setLoadingState(true);
        } else {
            setRefreshState(true);
        }
    }

    public void stopProgress() {
        setLoadingState(false);
        setRefreshState(false);
    }

    private void setLoadingState(final boolean loading) {
        if(contentView != null && indicatorView != null) {
            if(loading) {
                contentView.setVisibility(View.INVISIBLE);
                indicatorView.setVisibility(View.VISIBLE);
            } else {
                contentView.setVisibility(View.VISIBLE);
                indicatorView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setRefreshState(final boolean refreshing) {
        if (optionMenu != null) {
            final MenuItem refreshItem = optionMenu
                    .findItem(refreshViewId);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }
}
