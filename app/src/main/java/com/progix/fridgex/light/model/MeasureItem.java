package com.progix.fridgex.light.model;


public final class MeasureItem {

    private String product;

    private String cup250;

    private String cup200;

    private String tbsp;

    private String tsp;

    private String onepcs;


    public String getProduct() {
        return this.product;
    }

    public void setProduct(String var1) {
        this.product = var1;
    }


    public String getCup250() {
        return this.cup250;
    }

    public void setCup250(String var1) {
        this.cup250 = var1;
    }


    public String getCup200() {
        return this.cup200;
    }

    public void setCup200(String var1) {
        this.cup200 = var1;
    }


    public String getTbsp() {
        return this.tbsp;
    }

    public void setTbsp(String var1) {
        this.tbsp = var1;
    }


    public String getTsp() {
        return this.tsp;
    }

    public void setTsp(String var1) {
        this.tsp = var1;
    }


    public String getOnepcs() {
        return this.onepcs;
    }

    public void setOnepcs(String var1) {
        this.onepcs = var1;
    }

    public MeasureItem(String product, String cup250, String cup200, String tbsp, String tsp, String onepcs) {
        this.product = product;
        this.cup250 = cup250;
        this.cup200 = cup200;
        this.tbsp = tbsp;
        this.tsp = tsp;
        this.onepcs = onepcs;
    }
}
