/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.listener

interface AdListener {
    fun onAdLoadFailed(exception: Exception)
    fun onAdLoaded()
    fun onAdClosed()
    fun onAdShown()
    fun onApplicationLeft()
}
