/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.base

import android.content.Context
import android.util.Log
import androidx.annotation.RestrictTo
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import com.lazygeniouz.house.ads.helper.CoroutineScopeHandler

/**
 * Base class to handle CoroutineScopes for other sub-classes
 * & some other misc things.
 *
 * Should not be used outside the library scope.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class BaseAd(val context: Context) {

    private val scopeHandler by lazy { CoroutineScopeHandler() }

    init {
        if (context is FragmentActivity)
            context.lifecycle
                    .addObserver(object : LifecycleObserver {
                        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                        fun destroy() {
                            dispose()
                        }
                    })
        else Log.e(TAG, "The supplied Context is not a FragmentActivity instance." +
                "\nPlease make sure to call dispose() method on your Ad instance.")
    }

    /**
     * A coroutine launcher method,
     * implemented by the subclasses' **configureAds()** method
     */
    internal fun launch(task: suspend (() -> Unit)) {
        scopeHandler.launch(task)
    }

    /**
     * This method properly disposes / cancels all the coroutines
     * created by the internal [CoroutineScopeHandler].
     *
     * If the context is not a FragmentActivity instance,
     * make sure to call **[dispose]** manually
     */
    fun dispose() = scopeHandler.dispose()

    internal suspend fun getImageFromNetwork(url: String): ImageResult {
        return getImageLoader(context)
                .execute(ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false)
                        .build())
    }

    private fun getImageLoader(context: Context) = ImageLoader.Builder(context)
            .crossfade(true)
            .build()

    companion object {
        private const val TAG = "HouseAds"
    }
}