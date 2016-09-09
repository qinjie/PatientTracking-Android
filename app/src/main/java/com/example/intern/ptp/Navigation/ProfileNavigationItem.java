package com.example.intern.ptp.Navigation;

public class ProfileNavigationItem implements NavigationItem {

    private String name;
    private String mail;

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_PROFILE;
    }

    public ProfileNavigationItem(String name, String mail) {
        this.name = name;
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }
}
