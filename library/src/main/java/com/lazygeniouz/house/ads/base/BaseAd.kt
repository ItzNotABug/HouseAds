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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class BaseAd(val context: Context) {

    private var scopeHandler = CoroutineScopeHandler()

    init {
        if (context is FragmentActivity) context.lifecycle.addObserver(HostActivityObserver())
        else Log.e(TAG, "The supplied Context is not a FragmentActivity instance." +
                "\nPlease make sure to call `dispose()` method on your Ad instance.")
    }

    fun launch(task: suspend (() -> Unit)) {
        scopeHandler.launch(task)
    }

    // If the context is not a FragmentActivity instance,
    // make sure to call dispose manually
    fun dispose() {
        scopeHandler.dispose()
    }

    suspend fun getImageFromNetwork(url: String): ImageResult {
        return getImageLoader(context)
                .execute(ImageRequest.Builder(context)
                        .data(url)
                        .allowHardware(false)
                        .build())
    }

    private fun getImageLoader(context: Context) = ImageLoader.Builder(context)
            .crossfade(true)
            .build()


    private inner class HostActivityObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun destroy() {
            dispose()
        }
    }

    companion object {
        private val TAG = "HouseAds" + BaseAd::class.java.simpleName
    }
}