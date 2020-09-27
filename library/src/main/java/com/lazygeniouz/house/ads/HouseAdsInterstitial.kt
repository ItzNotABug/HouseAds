/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

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
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.lazygeniouz.house.ads.base.BaseAd
import com.lazygeniouz.house.ads.extension.getDrawableUriAsString
import com.lazygeniouz.house.ads.extension.hasDrawableSign
import com.lazygeniouz.house.ads.extension.hasHttpSign
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
    private val jsonHelper: JsonHelper = JsonHelper()

    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = jsonHelper.getJsonFromRaw(context, rawFile)
    }

    fun setAdListener(listener: AdListener): HouseAdsInterstitial {
        adListener = listener
        return this
    }

    fun usePalette(value: Boolean): HouseAdsInterstitial {
        usePalette = value
        return this
    }

    fun hideNavigationBar(value: Boolean): HouseAdsInterstitial {
        hideNavigation = value
        return this
    }

    fun setCloseButtonColor(
            backgroundColor: Int = Color.WHITE,
            iconTint: Int = Color.BLACK): HouseAdsInterstitial {
        closeButtonBackgroundColor = backgroundColor
        closeIconColor = iconTint
        return this
    }

    /**
     * Google AdMob has recently applied this
     * behavior to their Interstitial Ads.
     *
     * @param exit = If true, Interstitial Ad can be closed by a back press.
     * Else the user will have to click on the (X) button
     */
    fun exitOnBackPress(exit: Boolean): HouseAdsInterstitial {
        exitOnBackPress = exit
        return this
    }

    fun loadAds() {
        if (!isUsingRawRes) {
            require(jsonUrl.trim().isNotEmpty()) { context.getString(R.string.error_url_blank) }
            if (jsonRawResponse.isEmpty()) {
                launch {
                    val result = jsonHelper.getJsonObject(jsonUrl)
                    if (result.trim().isNotEmpty()) {
                        jsonRawResponse = result
                        configureAds(result)
                    } else adListener?.onAdLoadFailed(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

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

                if (jsonObject.optString("app_adType") == "interstitial") {
                    val interstitialModal = InterstitialModal()
                    interstitialModal.interstitialImageUrl = jsonObject.optString("app_interstitial_url")
                    interstitialModal.packageOrUrl = jsonObject.optString("app_uri")
                    modalArrayList.add(interstitialModal)
                }
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
                context.getDrawableUriAsString(modal.interstitialImageUrl)!!
            } else modal.interstitialImageUrl

            launch {
                when (val result = getImageFromNetwork(imageUrlToLoad)) {
                    is SuccessResult -> {
                        bitmap = (result.drawable as BitmapDrawable).bitmap
                        if (usePalette && bitmap != null) {
                            //val palette = Palette.from(bitmap!!).generate()
                            paletteColor = getDominantColor(bitmap!!)
                        }
                        adListener?.onAdLoaded()
                        isAdLoaded = true
                    }
                    is ErrorResult -> {
                        adListener?.onAdLoadFailed(Exception(result.throwable))
                        isAdLoaded = false
                    }
                }
                packageName = modal.packageOrUrl
            }
        }
    }

    private fun getDominantColor(bitmap: Bitmap): Int {
        val newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
        val color = newBitmap.getPixel(0, 0)
        newBitmap.recycle()
        return color
    }

    fun show() {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(0, 0)
    }


    @Suppress("unused")
    fun show(@NonNull @AnimRes enterAnim: Int, @NonNull @AnimRes exitAnim: Int) {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(enterAnim, exitAnim)
    }

    class InterstitialActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            fullscreen()
            super.onCreate(savedInstanceState)
            adListener?.onAdShown()
            setContentView(R.layout.house_ads_interstitial_layout)
            val parent = findViewById<RelativeLayout>(R.id.houseAds_interstitial_parent)
            val imageView = findViewById<ImageView>(R.id.houseAds_interstitial_image)
            val button = findViewById<ImageButton>(R.id.houseAds_interstitial_button_close)

            parent.setBackgroundColor(paletteColor)

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
                setColor(closeButtonBackgroundColor)
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
                background = drawable
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

    private companion object {
        private var lastLoaded = 0
        private var adListener: AdListener? = null
        private var isAdLoaded = false
        private var bitmap: Bitmap? = null
        private var exitOnBackPress: Boolean = false
        private var paletteColor: Int = Color.BLACK
        private var closeButtonBackgroundColor = Color.WHITE
        private var closeIconColor = Color.BLACK
        private var usePalette: Boolean = false
        private var hideNavigation: Boolean = true
        private var packageName: String? = null
        private val TAG = HouseAdsInterstitial::class.java.simpleName
    }
}
