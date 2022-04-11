package com.progix.fridgex.light.model;

public final class InfoItem {
    private String name;
    private String value;
    private int image;

    public String getName() {
        return this.name;
    }

    public void setName(String var1) {
        this.name = var1;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String var1) {
        this.value = var1;
    }

    public int getImage() {
        return this.image;
    }

    public void setImage(int var1) {
        this.image = var1;
    }

    public InfoItem(String name, String value, int image) {
        this.name = name;
        this.value = value;
        this.image = image;
    }
}
