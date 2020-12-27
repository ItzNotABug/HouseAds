/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

@file:Suppress("unused")

package com.lazygeniouz.house.ads

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.AnimRes
import androidx.annotation.NonNull
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.lazygeniouz.house.ads.base.BaseAd
import com.lazygeniouz.house.ads.extension.*
import com.lazygeniouz.house.ads.helper.JsonHelper
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.modal.InterstitialModal
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HouseAdsInterstitial(context: Context, private val jsonUrl: String) : BaseAd(context) {

    private var isUsingRawRes = false
    private var jsonRawResponse = ""
    private var jsonLocalRawResponse = ""

    /**
     * Secondary constructor if you want to use a custom Json File from Raw folder
     */
    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = JsonHelper.getJsonFromRaw(context, rawFile)
    }

    /**
     * Set an [AdListener] to listen to Ad events
     *
     * Example: [AdListener.onAdLoaded], [AdListener.onAdFailedToLoad], etc
     */
    fun setAdListener(listener: AdListener): HouseAdsInterstitial {
        adListener = listener
        return this
    }

    /**
     * Set whether to use Palette API to color UI elements
     */
    fun usePalette(value: Boolean): HouseAdsInterstitial {
        usePalette = value
        return this
    }

    /**
     * Set whether to show / hide the system Navigation Bar
     * when the Interstitial Ad is displayed
     */
    fun hideNavigationBar(value: Boolean): HouseAdsInterstitial {
        hideNavigation = value
        return this
    }

    /**
     * Set the background & the icon color of the
     * Close Button **(X)** on the Interstitial Ad
     */
    fun setCloseButtonColor(
            backgroundColor: Int = Color.WHITE,
            iconTint: Int = Color.BLACK): HouseAdsInterstitial {
        closeButtonBackgroundColor = backgroundColor
        closeIconColor = iconTint
        return this
    }

    /**
     * Set the Close Button's **(X)** location
     *
     * Can either be [LOCATION.LOCATION_LEFT]
     *
     * or [LOCATION.LOCATION_RIGHT]
     */
    fun setCloseButtonLocation(location: LOCATION): HouseAdsInterstitial {
        closeButtonLocation = location
        return this
    }

    /**
     * Google AdMob has recently applied this
     * behavior to their Interstitial Ads.
     *
     * If **true**, Interstitial Ad can be closed by a BackPress.
     * Else the user will have to click on the Close Button **(X)**
     */
    fun exitOnBackPress(exit: Boolean): HouseAdsInterstitial {
        exitOnBackPress = exit
        return this
    }

    /**
     * Alright, lets load the Ads
     */
    fun loadAds() {
        isAdLoaded = false
        if (!isUsingRawRes) {
            require(jsonUrl.trim().isNotEmpty()) { context.getString(R.string.error_url_blank) }
            if (jsonRawResponse.isEmpty()) {
                launch {
                    val result = JsonHelper.getJsonObject(jsonUrl)
                    if (result.trim().isNotEmpty()) {
                        jsonRawResponse = result
                        configureAds(result)
                    } else adListener?.onAdFailedToLoad(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

    /**
     * Show the loaded Ad
     */
    fun show() {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(0, 0)
    }

    /**
     * Show the loaded Ad with activity transitions
     */
    fun show(@NonNull @AnimRes enterAnim: Int, @NonNull @AnimRes exitAnim: Int) {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is Activity) context.overridePendingTransition(enterAnim, exitAnim)
        else Log.d(TAG, "show(enterAnim, exitAnim) cannot be used because the Context is not an instance of Activity")
    }

    /**
     * Check if the Interstitial Ad is loaded
     */
    fun isAdLoaded(): Boolean {
        return isAdLoaded
    }

    private fun configureAds(jsonResponse: String) {
        val modalArrayList = ArrayList<InterstitialModal>()

        try {
            val rootObject = JSONObject(jsonResponse)
            val array = rootObject.optJSONArray("apps")!!
            for (childObject in 0 until array.length()) {
                val jsonObject = array.getJSONObject(childObject)
                if (jsonObject.optString("app_adType") == "interstitial")
                    modalArrayList.add(getInterstitialModal(jsonObject))
            }
        } catch (jsonException: JSONException) {
            jsonException.printStackTrace()
        }

        val modal = modalArrayList[lastLoaded]
        if (modalArrayList.size > 0) {
            if (lastLoaded == modalArrayList.size - 1) lastLoaded = 0
            else lastLoaded++

            if (!isUsingRawRes) {
                require(!(modal.interstitialImageUrl.isEmpty() || !modal.interstitialImageUrl.hasHttpSign))
                { context.getString(R.string.error_interstitial_image_url_null) }
            } else {
                when {
                    modal.interstitialImageUrl.trim().hasHttpSign -> Log.d(TAG, "ImageUrl starts with `http://`")
                    modal.interstitialImageUrl.trim().hasDrawableSign -> Log.d(TAG, "ImageUrl is a local drawable")
                    else -> throw IllegalArgumentException(context.getString(R.string.error_raw_resource_interstitial_image_null))
                }
            }

            val imageUrlToLoad: String = if (modal.interstitialImageUrl.hasDrawableSign) {
                context.getDrawableUriAsString(modal.interstitialImageUrl)
            } else modal.interstitialImageUrl

            launch {
                when (val result = getImageFromNetwork(imageUrlToLoad)) {
                    is SuccessResult -> {
                        bitmap = (result.drawable as BitmapDrawable).bitmap
                        if (usePalette && bitmap != null) paletteColor = bitmap!!.getDominantColorForInterstitial()
                        adListener?.onAdLoaded()
                        isAdLoaded = true
                    }
                    is ErrorResult -> {
                        adListener?.onAdFailedToLoad(Exception(result.throwable))
                        isAdLoaded = false
                    }
                }
                packageName = modal.packageOrUrl
            }
        }
    }

    class InterstitialActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            fullscreen()
            super.onCreate(savedInstanceState)

            adListener?.onAdShown()
            setContentView(R.layout.house_ads_interstitial_layout)
            val parent = findViewById<RelativeLayout>(R.id.houseAds_interstitial_parent)
            val imageView = findViewById<ImageView>(R.id.houseAds_interstitial_image)
            val cardView = findViewById<CardView>(R.id.houseAds_interstitial_button_close_card)
            val button = findViewById<ImageButton>(R.id.houseAds_interstitial_button_close)

            parent.setBackgroundColor(paletteColor)
            cardView.apply {
                if (closeButtonLocation == LOCATION.LOCATION_RIGHT)
                    layoutParams = (layoutParams as RelativeLayout.LayoutParams)
                            .apply {
                                addRule(RelativeLayout.ALIGN_PARENT_END)
                            }
            }

            imageView.apply {
                setImageBitmap(bitmap)
                setOnClickListener {
                    isAdLoaded = false
                    if (packageName!!.hasHttpSign) {
                        val packageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(packageName))
                        packageIntent.setPackage("com.android.chrome")
                        if (packageIntent.resolveActivity(packageManager) != null)
                            startActivity(packageIntent)
                        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageName)))

                        adListener?.onApplicationLeft()
                        finish()
                    } else {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName!!)))
                            adListener?.onApplicationLeft()
                            finish()
                        } catch (e: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName!!)))
                            adListener?.onApplicationLeft()
                            finish()
                        }
                    }
                }
            }

            button.apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    setColor(closeButtonBackgroundColor)
                }
                setColorFilter(closeIconColor, PorterDuff.Mode.SRC_ATOP)
                setOnClickListener {
                    finish()
                    isAdLoaded = false
                    adListener?.onAdClosed()
                }
            }
        }

        override fun onBackPressed() {
            if (exitOnBackPress) {
                isAdLoaded = false
                adListener?.onAdClosed()
                finish()
            }
        }

        private fun fullscreen() {
            if (Build.VERSION.SDK_INT >= 19) {
                val decorView: View = window.decorView
                val uiOptions: Int = if (hideNavigation) View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                else View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                decorView.systemUiVisibility = uiOptions
            }
        }
    }

    // to be used inside the nested activity
    private companion object {
        private var lastLoaded = 0
        private var adListener: AdListener? = null
        private var isAdLoaded = false
        private var bitmap: Bitmap? = null
        private var exitOnBackPress: Boolean = false
        private var paletteColor: Int = Color.BLACK
        private var closeButtonBackgroundColor = Color.WHITE
        private var closeButtonLocation = LOCATION.LOCATION_LEFT
        private var closeIconColor = Color.BLACK
        private var usePalette: Boolean = false
        private var hideNavigation: Boolean = true
        private var packageName: String? = null
        private val TAG = HouseAdsInterstitial::class.java.simpleName
    }

    enum class LOCATION {
        LOCATION_LEFT,
        LOCATION_RIGHT
    }
}
