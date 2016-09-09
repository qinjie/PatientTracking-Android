package com.example.intern.ptp.Navigation;

public interface NavigationItem {
    public static final int VIEWTYPE_PROFILE = 0;
    public static final int VIEWTYPE_PRIMARY = 1;
    public static final int VIEWTYPE_SECONDARY = 2;
    public static final int VIEWTYPE_DIVIDER = 3;

    public int getType();
}
