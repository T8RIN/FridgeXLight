package com.progix.fridgex.light.model;

public final class RecyclerSortItem {
    private double amount;
    private int time;
    private double cal;
    private double prot;
    private double fats;
    private double carboh;

    private RecipeItem recipeItem;

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double var1) {
        this.amount = var1;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int var1) {
        this.time = var1;
    }

    public double getCal() {
        return this.cal;
    }

    public void setCal(double var1) {
        this.cal = var1;
    }

    public double getProt() {
        return this.prot;
    }

    public void setProt(double var1) {
        this.prot = var1;
    }

    public double getFats() {
        return this.fats;
    }

    public void setFats(double var1) {
        this.fats = var1;
    }

    public double getCarboh() {
        return this.carboh;
    }

    public void setCarboh(double var1) {
        this.carboh = var1;
    }


    public RecipeItem getRecipeItem() {
        return this.recipeItem;
    }

    public void setRecipeItem(RecipeItem var1) {
        this.recipeItem = var1;
    }

    public RecyclerSortItem(double amount, int time, double cal, double prot, double fats, double carboh, RecipeItem recipeItem) {
        this.amount = amount;
        this.time = time;
        this.cal = cal;
        this.prot = prot;
        this.fats = fats;
        this.carboh = carboh;
        this.recipeItem = recipeItem;
    }
}
