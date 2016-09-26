package com.example.intern.ptp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.intern.ptp.Preferences;

public class UserManager {

    public static String getName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        return  prefs.getString("username", "");
    }

    public static String getId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        return prefs.getString("id", "");
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Preferences.SharedPreferencesTag, Preferences.SharedPreferences_ModeTag);

        return prefs.getString("email", "");
    }

}
