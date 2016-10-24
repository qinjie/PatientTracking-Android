package com.example.intern.ptp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class AnimUtils {

    public static final int ANIMATION_SHORT = 200;

    public static void crossFade(final View fromView, final View toView, int animationTime) {
        fromView.animate()
                .alpha(0f)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fromView.setVisibility(View.INVISIBLE);
                        fromView.setAlpha(1f);
                    }
                });

        toView.setAlpha(0f);
        toView.setVisibility(View.VISIBLE);

        toView.animate()
                .alpha(1f)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toView.setVisibility(View.VISIBLE);
                        toView.setAlpha(1f);
                    }
                });

    }
}