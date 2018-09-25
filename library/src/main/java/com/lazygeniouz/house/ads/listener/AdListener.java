/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.listener;

public interface AdListener {

    void onAdLoaded();
    void onAdClosed();
    void onAdShown();
    void onApplicationLeft();
}
