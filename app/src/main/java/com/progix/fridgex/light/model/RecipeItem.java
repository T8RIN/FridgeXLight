package com.progix.fridgex.light.model;

public final class RecipeItem {
    private int image;
    private int indicator;

    private String recipeName;

    private String time;

    private String xOfY;

    public int getImage() {
        return this.image;
    }

    public void setImage(int var1) {
        this.image = var1;
    }

    public int getIndicator() {
        return this.indicator;
    }

    public void setIndicator(int var1) {
        this.indicator = var1;
    }


    public String getRecipeName() {
        return this.recipeName;
    }

    public void setRecipeName(String var1) {
        this.recipeName = var1;
    }


    public String getTime() {
        return this.time;
    }

    public void setTime(String var1) {
        this.time = var1;
    }


    public String getXOfY() {
        return this.xOfY;
    }

    public void setXOfY(String var1) {
        this.xOfY = var1;
    }

    public RecipeItem(int image, int indicator, String recipeName, String time, String xOfY) {
        this.image = image;
        this.indicator = indicator;
        this.recipeName = recipeName;
        this.time = time;
        this.xOfY = xOfY;
    }
}
