package com.progix.fridgex.light.model;

public final class NavItem {

    private String name;
    private int image;


    public String getName() {
        return this.name;
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public int getImage() {
        return this.image;
    }

    public void setImage(int var1) {
        this.image = var1;
    }

    public NavItem(String name, int image) {
        this.name = name;
        this.image = image;
    }
}
