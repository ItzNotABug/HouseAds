/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.listener

interface AdListener {
    fun onAdLoaded()
    fun onAdClosed()
    fun onAdShown()
    fun onApplicationLeft()
    fun onAdFailedToLoad(exception: Exception)
}
