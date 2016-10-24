package com.example.intern.ptp.views.navigation;

public class SecondaryNavigationItem implements NavigationItem {

    private String id;
    private String label;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_SECONDARY;
    }

    public SecondaryNavigationItem(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
