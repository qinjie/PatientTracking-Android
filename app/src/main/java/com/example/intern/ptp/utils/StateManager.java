package com.example.intern.ptp.utils;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.intern.ptp.R;
import com.example.intern.ptp.views.widgets.ErrorView;

public class StateManager {

    private Menu optionMenu;

    private View contentView;
    private View progressView;
    private ErrorView errorView;

    public void showProgress(boolean isInitialLoading) {
        if (isInitialLoading) {
            setLoadingState(true);
        } else {
            setRefreshState(true);
        }
    }

    public void showContent() {
        if(errorView != null) {
            errorView.setVisibility(View.INVISIBLE);
        }

        if(progressView.getVisibility() == View.VISIBLE) {
            setLoadingState(false);
        } else {
            setRefreshState(false);
        }
    }

    public void showContentDelayed(int delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                StateManager.this.showContent();
            }
        }, delay);
    }

    public void showError(String message) {
        if (errorView == null) {
            return;
        }

        errorView.setError(message);
        errorView.setVisibility(View.VISIBLE);

        if (progressView != null) {
            progressView.setVisibility(View.INVISIBLE);
        }

        if (contentView != null) {
            contentView.setVisibility(View.INVISIBLE);
        }
    }

    private void setLoadingState(final boolean loading) {
        if (contentView != null && progressView != null) {
            if (loading) {
                AnimUtils.crossFade(contentView, progressView, AnimUtils.ANIMATION_SHORT);
            } else {
                AnimUtils.crossFade(progressView, contentView, AnimUtils.ANIMATION_SHORT);
            }
        }
    }

    private void setRefreshState(final boolean refreshing) {
        if (optionMenu != null) {
            final MenuItem refreshItem = optionMenu
                    .findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }


    public void setMenu(Menu optionMenu) {
        this.optionMenu = optionMenu;
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    public void setProgressView(View progressView) {
        this.progressView = progressView;
    }

    public void setErrorView(ErrorView errorView) {
        this.errorView = errorView;
    }
}
