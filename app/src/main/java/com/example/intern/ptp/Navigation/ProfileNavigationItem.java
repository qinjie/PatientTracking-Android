package com.example.intern.ptp.Navigation;

public class ProfileNavigationItem implements NavigationItem {
    String id;
    private String name;
    private String mail;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getType() {
        return NavigationItem.VIEWTYPE_PROFILE;
    }

    public ProfileNavigationItem(String id, String name, String mail) {
        this.id = id;
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
