package com.example.intern.ptp.Navigation;

public class PrimaryNavigationItem implements NavigationItem {
    private String fontIcon;
    private String label;
    private String additionalInformation;

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_PRIMARY;
    }

    public PrimaryNavigationItem(String fontIcon, String label, String additionalInformation) {
        this.fontIcon = fontIcon;
        this.label = label;
        this.additionalInformation = additionalInformation;
    }

    public String getFontIcon() {
        return fontIcon;
    }

    public String getLabel() {
        return label;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
