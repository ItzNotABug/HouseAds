/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

@file:Suppress("unused")

package com.lazygeniouz.house.ads

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.lazygeniouz.house.ads.base.BaseAd
import com.lazygeniouz.house.ads.extension.*
import com.lazygeniouz.house.ads.helper.JsonHelper
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.modal.DialogModal
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Show a custom Dialog Ad.
 *
 * **See:**  [HouseAdsDialog](https://github.com/ItzNotABug/HouseAds#houseadsdialog)
 */

class HouseAdsDialog(context: Context, private val jsonUrl: String) : BaseAd(context) {

    private var jsonRawResponse = ""
    private var jsonLocalRawResponse = ""
    private var showHeader = true
    private var hideIfAppInstalled = true
    private var usePalette = true
    private var cardCorner = 25
    private var isAllCaps: Boolean = true
    private var callToActionButtonCorner = 25

    private var lastLoaded = 0
    private var isAdLoaded = false
    private var isUsingRawRes = false

    private var mAdListener: AdListener? = null
    private var dialog: AlertDialog? = null

    /**
     * Secondary constructor if you want to use a custom Json File from Raw folder
     */
    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = JsonHelper.getJsonFromRaw(context, rawFile)
    }

    /**
     * Set whether to show Header Image if available
     */
    fun showHeaderIfAvailable(showHeader: Boolean): HouseAdsDialog {
        this.showHeader = showHeader
        return this
    }

    /**
     * Set the Dialog Ad's corner radius
     */
    fun setCardCorners(corners: Int): HouseAdsDialog {
        this.cardCorner = corners
        return this
    }

    /**
     * Set the Call to Action Button's color
     */
    fun setCtaCorner(corner: Int): HouseAdsDialog {
        this.callToActionButtonCorner = corner
        return this
    }

    /**
     * Set Call to Action Button's Text in All Caps
     */
    fun ctaAllCaps(isAllCaps: Boolean): HouseAdsDialog {
        this.isAllCaps = isAllCaps
        return this
    }

    /**
     * Set an [AdListener] to listen to Ad events
     *
     * Example: [AdListener.onAdLoaded], [AdListener.onAdFailedToLoad], etc
     */
    fun setAdListener(listener: AdListener): HouseAdsDialog {
        this.mAdListener = listener
        return this
    }

    /**
     * Set whether to show the Dialog Ad if,
     *
     * the Current Ad is of App Type (package name) and
     * the App is already installed on the user's device
     */
    fun hideIfAppInstalled(hide: Boolean): HouseAdsDialog {
        this.hideIfAppInstalled = hide
        return this
    }

    /**
     * Set whether to use Palette API to color UI elements
     */
    fun usePalette(usePalette: Boolean): HouseAdsDialog {
        this.usePalette = usePalette
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
                    } else mAdListener?.onAdFailedToLoad(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

    /**
     * Show the loaded Ad
     */
    fun showAd() = dialog?.show()

    /**
     * Check if the Ad is loaded
     */
    fun isAdLoaded(): Boolean {
        return isAdLoaded
    }

    private fun configureAds(response: String) {
        val builder = AlertDialog.Builder(context)
        val dialogModalList = ArrayList<DialogModal>()

        try {
            val rootObject = JSONObject(response)
            val jsonArray = rootObject.optJSONArray("apps")!!

            for (childObject in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(childObject)

                if (hideIfAppInstalled && !jsonObject.optString("app_uri").hasHttpSign &&
                        context.isAppInstalled(jsonObject.optString("app_uri")))
                    jsonArray.remove(childObject)
                else {
                    if (jsonObject.optString("app_adType") == "dialog")
                        dialogModalList.add(getDialogModal(jsonObject))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (dialogModalList.size > 0) {
            val dialogModal = dialogModalList[lastLoaded]
            if (lastLoaded >= dialogModalList.size - 1) lastLoaded = 0
            else lastLoaded++

            val view = View.inflate(context, R.layout.house_ads_dialog_layout, null)
            val iconUrl = dialogModal.iconUrl!!
            val largeImageUrl = dialogModal.largeImageUrl!!
            val appTitle = dialogModal.appTitle!!
            val appDescription = dialogModal.appDesc!!

            if (!isUsingRawRes) {
                require(!(iconUrl.trim().isEmpty() || !iconUrl.trim().hasHttpSign)) {
                    context.getString(R.string.error_icon_url_null)
                }

                require(!(largeImageUrl.trim().isNotEmpty() && !largeImageUrl.trim().hasHttpSign)) {
                    context.getString(R.string.error_header_image_url_null)
                }

                require(!(appTitle.trim().isEmpty() || appDescription.trim().isEmpty())) {
                    context.getString(R.string.error_title_description_null)
                }
            } else {
                if (iconUrl.trim().isNotEmpty()) {
                    when {
                        iconUrl.trim().hasHttpSign -> Log.d(TAG, "App Logo param starts with `http://`")
                        iconUrl.trim().hasDrawableSign -> Log.d(TAG, "App Logo param is a local drawable")
                        else -> throw IllegalArgumentException(context.getString(R.string.error_raw_resource_icon_null))
                    }
                }
                if (largeImageUrl.trim().isNotEmpty()) {
                    when {
                        largeImageUrl.trim().hasHttpSign -> Log.d(TAG, "App Header param starts with `http://`")
                        largeImageUrl.trim().hasDrawableSign -> Log.d(TAG, "App Header param is a local drawable")
                        else -> throw IllegalArgumentException(context.getString(R.string.error_raw_resource_header_image_null))
                    }
                }
            }

            val cardView = view.findViewById<CardView>(R.id.houseAds_card_view)
            cardView.radius = cardCorner.toFloat()

            val callToActionButton = view.findViewById<Button>(R.id.houseAds_cta)
            val gradientDrawable = callToActionButton.background as GradientDrawable
            gradientDrawable.cornerRadius = callToActionButtonCorner.toFloat()

            val icon = view.findViewById<ImageView>(R.id.houseAds_app_icon)
            val headerImage = view.findViewById<ImageView>(R.id.houseAds_header_image)
            val title = view.findViewById<TextView>(R.id.houseAds_title)
            val description = view.findViewById<TextView>(R.id.houseAds_description)
            val ratings = view.findViewById<RatingBar>(R.id.houseAds_rating)
            val price = view.findViewById<TextView>(R.id.houseAds_price)

            launch {
                val iconUrlToLoad: String = if (iconUrl.hasDrawableSign) context.getDrawableUriAsString(iconUrl)
                else iconUrl

                when (val result = getImageFromNetwork(iconUrlToLoad)) {
                    is SuccessResult -> {
                        icon.setImageDrawable(result.drawable)
                        isAdLoaded = true

                        if (icon.visibility == View.GONE) icon.visibility = View.VISIBLE
                        var dominantColor = ContextCompat.getColor(context, R.color.colorAccent)
                        if (usePalette) dominantColor = (icon.drawable as BitmapDrawable).bitmap.getDominantColor()

                        val drawable = callToActionButton.background as GradientDrawable
                        drawable.setColor(dominantColor)

                        if (dialogModal.getRating() > 0) {
                            ratings.rating = dialogModal.getRating()
                            val ratingsDrawable = ratings.progressDrawable
                            DrawableCompat.setTint(ratingsDrawable, dominantColor)
                        } else ratings.visibility = View.GONE
                    }
                    is ErrorResult -> {
                        isAdLoaded = false
                        mAdListener?.onAdFailedToLoad(Exception("The Icon Uri: $iconUrlToLoad could not be fetched. More Info: ${result.throwable}"))
                        icon.visibility = View.GONE
                    }
                }
                if (largeImageUrl.trim().isNotEmpty() && showHeader) {
                    val largeImageUrlToLoad: String = if (largeImageUrl.hasDrawableSign) context.getDrawableUriAsString(largeImageUrl)
                    else largeImageUrl

                    when (val result = getImageFromNetwork(largeImageUrlToLoad)) {
                        is SuccessResult -> {
                            headerImage.setImageDrawable(result.drawable)
                            headerImage.visibility = View.VISIBLE
                        }

                        is ErrorResult -> {
                            headerImage.visibility = View.GONE
                        }
                    }
                } else headerImage.visibility = View.GONE

                title.text = dialogModal.appTitle
                description.text = dialogModal.appDesc
                callToActionButton.text = dialogModal.callToActionButtonText
                callToActionButton.isAllCaps = isAllCaps
                if (dialogModal.price!!.trim().isEmpty()) price.visibility = View.GONE
                else price.text = String.format(context.getString(R.string.price_format), dialogModal.price)

                builder.setView(view)
                dialog = builder.create()
                        .apply {
                            window?.setBackgroundDrawableResource(android.R.color.transparent)
                            setOnShowListener { mAdListener?.onAdShown() }
                            setOnDismissListener { mAdListener?.onAdClosed() }
                        }

                // Calling this here because previous implementation was'nt correct
                // and the first call to show() would never show the dialog.
                if (isAdLoaded) mAdListener?.onAdLoaded()

                callToActionButton.setOnClickListener {
                    dialog!!.dismiss()
                    val packageOrUrl = dialogModal.packageOrUrl
                    if (packageOrUrl!!.trim().startsWith("http")) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)))
                        mAdListener?.onApplicationLeft()
                    } else {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageOrUrl")))
                            mAdListener?.onApplicationLeft()
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageOrUrl")))
                            mAdListener?.onApplicationLeft()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = HouseAdsDialog::class.java.simpleName
    }
}




