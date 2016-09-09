package com.example.intern.ptp.Navigation;

public class SecondaryNavigationItem implements NavigationItem {

    private String label;

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_SECONDARY;
    }

    public SecondaryNavigationItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
