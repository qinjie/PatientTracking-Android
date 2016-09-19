package com.example.intern.ptp.Navigation;

public class PrimaryNavigationItem implements NavigationItem {
    private String id;
    private String fontIcon;
    private String label;
    private String additionalInformation;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_PRIMARY;
    }

    public PrimaryNavigationItem(String id, String fontIcon, String label, String additionalInformation) {
        this.id = id;
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

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
