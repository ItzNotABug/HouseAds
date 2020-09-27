/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.listener

import android.view.View

interface NativeAdListener {
    fun onAdLoaded()
    fun onAdLoadFailed(exception: Exception)
}

interface NativeActionListener {
    fun onClick(view: View)
}
