/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.modal;

public class DialogModal {
    private String iconUrl;
    private String appTitle;
    private String appDesc;
    private String largeImageUrl;
    private String packageNameOrUrl;
    private String ctaText;
    private String price;
    private String rating;

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setLargeImageUrl(String largeImageUrl) {
        this.largeImageUrl = largeImageUrl;
    }

    public void setPackageOrUrl(String pgnurl) {
        this.packageNameOrUrl = pgnurl;
    }

    public void setPrice(String val) {
        this.price = val;
    }

    public void setRating(String val) {
        this.rating = val;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public String getLargeImageUrl() {
        return largeImageUrl;
    }

    public String getPackageOrUrl() {
        return packageNameOrUrl;
    }

    public String getCtaText() {
        return ctaText;
    }

    public String getPrice() {
        return price;
    }

    public float getRating() {
        return Float.parseFloat(rating);
    }
}
