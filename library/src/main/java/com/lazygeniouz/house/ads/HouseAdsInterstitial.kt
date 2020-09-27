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
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.AnimRes
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

    fun setAdListener(adListener: AdListener): HouseAdsInterstitial {
        mAdListener = adListener
        return this
    }

    fun loadAd() {
        if (!isUsingRawRes) {
            require(jsonUrl.trim().isNotEmpty()) { context.getString(R.string.error_url_blank) }
            if (jsonRawResponse.isEmpty()) {
                launch {
                    val result = jsonHelper.getJsonObject(jsonUrl)
                    if (result.trim().isNotEmpty()) {
                        jsonRawResponse = result
                        configureAds(result)
                    } else mAdListener?.onAdLoadFailed(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

    fun isAdLoaded(): Boolean {
        return isAdLoaded
    }

    private fun configureAds(jsonResponse: String) = launch {
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
                require(!(modal.interstitialImageUrl.isEmpty() || !modal.interstitialImageUrl.hasHttpSign)) { context.getString(R.string.error_interstitial_image_url_null) }
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


            when (val result = getImageFromNetwork(imageUrlToLoad)) {
                is SuccessResult -> {
                    bitmap = (result.drawable as BitmapDrawable).bitmap
                    mAdListener?.onAdLoaded()
                    isAdLoaded = true
                }
                is ErrorResult -> {
                    mAdListener?.onAdLoadFailed(Exception(result.throwable))
                    isAdLoaded = false
                }
            }
            packageName = modal.packageOrUrl
        }
    }

    fun show(): HouseAdsInterstitial {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(0, 0)
        return this
    }


    @Suppress("unused")
    fun show(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int): HouseAdsInterstitial {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(enterAnim, exitAnim)
        return this
    }

    class InterstitialActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mAdListener?.onAdShown()

            setContentView(R.layout.house_ads_interstitial_layout)
            val imageView = findViewById<ImageView>(R.id.image)
            val button = findViewById<ImageButton>(R.id.button_close)

            imageView.setImageBitmap(bitmap)
            imageView.setOnClickListener {
                isAdLoaded = false
                if (packageName!!.hasHttpSign) {
                    val packageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(packageName))
                    packageIntent.setPackage("com.android.chrome")
                    if (packageIntent.resolveActivity(packageManager) != null)
                        startActivity(packageIntent)
                    else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageName)))

                    mAdListener?.onApplicationLeft()
                    finish()
                } else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName!!)))
                        mAdListener?.onApplicationLeft()
                        finish()
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName!!)))
                        mAdListener?.onApplicationLeft()
                        finish()
                    }
                }
            }
            button.setOnClickListener {
                finish()
                isAdLoaded = false
                mAdListener?.onAdClosed()
            }
        }

        override fun onBackPressed() {
            isAdLoaded = false
            mAdListener?.onAdClosed()
            finish()
        }
    }

    private companion object {
        private var lastLoaded = 0
        private var mAdListener: AdListener? = null
        private var isAdLoaded = false
        private var bitmap: Bitmap? = null
        private var packageName: String? = null
        private val TAG = HouseAdsInterstitial::class.java.simpleName
    }
}
