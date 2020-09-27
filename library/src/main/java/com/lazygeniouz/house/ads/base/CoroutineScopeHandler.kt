package com.lazygeniouz.house.ads.base

import android.util.Log
import androidx.annotation.RestrictTo
import kotlinx.coroutines.*

/**
 * The previous implementation included the Singleton Pattern,
 * however it didn't properly work.
 *
 * Class for using a CoroutineScope so that we don't have create/write a
 * new CoroutineScope every-time  BaseAd is initialized via Dialog, Interstitial or a Native.
 * Used to launch `configureAds()` inside a coroutine from children of BaseAd class
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class CoroutineScopeHandler {

    private val job: Job
    private val coroutineScope: CoroutineScope

    init {
        job = Job()
        coroutineScope = CoroutineScope(Dispatchers.Main + job)
        Log.d("HouseAds", "CoroutineScope Initialized")
    }

    fun launch(block: suspend (() -> Unit)) {
        coroutineScope.launch { block() }
    }

    fun dispose() {
        coroutineScope.coroutineContext.cancelChildren()
        coroutineScope.coroutineContext.cancel()
        Log.d("HouseAds", "CoroutineScope Disposed")
    }
}