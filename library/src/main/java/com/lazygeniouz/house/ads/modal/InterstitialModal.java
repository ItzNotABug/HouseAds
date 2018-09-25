/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.modal;

public class InterstitialModal {

    private String interstitialImageUrl;
    private String packageName;

    public void setInterstitialImageUrl(String interstitialImageUrl) {
        this.interstitialImageUrl = interstitialImageUrl;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInterstitialImageUrl() {
        return interstitialImageUrl;
    }

    public String getPackageName() {
        return packageName;
    }
}
