/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.listener;

import android.view.View;

@SuppressWarnings("unused")
public interface NativeAdListener {

    void onAdLoaded();
    void onAdLoadFailed(Exception e);

    interface CallToActionListener{
        void onCallToActionClicked(View view);
    }
}
