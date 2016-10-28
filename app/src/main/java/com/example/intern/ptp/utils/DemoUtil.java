package com.example.intern.ptp.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class DemoUtil {
    public static Drawable getResidentProfileDrawable(Context context, String residentId) {
        String profilePicture = "profile" + residentId;
        Drawable image = context.getDrawable(context.getResources().getIdentifier(profilePicture, "drawable", context.getPackageName()));

        if (image == null) {
            image = context.getDrawable(context.getResources().getIdentifier("profile31", "drawable", context.getPackageName()));
        }

        return image;
    }
}
