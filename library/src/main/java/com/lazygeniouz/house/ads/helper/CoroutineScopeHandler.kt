/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.helper

import androidx.annotation.RestrictTo
import com.lazygeniouz.house.ads.base.BaseAd
import kotlinx.coroutines.*

/**
 * The previous implementation included the Singleton Pattern,
 * but it didn't properly work.
 *
 * A helper class for using a [CoroutineScope] so that we don't have create / write a
 * new CoroutineScope every-time  BaseAd is initialized via Dialog, Interstitial or a Native.
 *
 * Used to launch **configureAds()** inside a coroutine from subclasses of BaseAd class
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class CoroutineScopeHandler {

    // SupervisorJob is a good fit here
    private val job by lazy { SupervisorJob() }
    private val coroutineScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.Main + job) }

    /**
     * A coroutine launcher method,
     * implemented by the subclasses of [BaseAd] class
     */
    fun launch(block: suspend (() -> Unit)) = coroutineScope.launch { block() }


    /**
     * This method properly disposes / cancels all the coroutines.
     *
     * If the context passed to **BaseAd** is a **FragmentActivity** instance,
     * then the BaseAd class handles this for you.
     *
     * If not, make sure to call **yourAdType.dispose()** manually.
     */
    fun dispose() = coroutineScope.apply {
        coroutineContext.cancelChildren()
        coroutineScope.coroutineContext.cancel()
    }
}