package com.example.intern.ptp.views.navigation;

public interface NavigationItem {
    int VIEWTYPE_PROFILE = 0;
    int VIEWTYPE_PRIMARY = 1;
    int VIEWTYPE_SECONDARY = 2;
    int VIEWTYPE_DIVIDER = 3;

    String getId();

    int getType();
}
