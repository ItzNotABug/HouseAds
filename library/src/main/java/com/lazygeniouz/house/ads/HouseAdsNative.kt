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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.palette.graphics.Palette
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.lazygeniouz.house.ads.base.BaseAd
import com.lazygeniouz.house.ads.extension.getDrawableUriAsString
import com.lazygeniouz.house.ads.extension.hasDrawableSign
import com.lazygeniouz.house.ads.extension.hasHttpSign
import com.lazygeniouz.house.ads.extension.isAppInstalled
import com.lazygeniouz.house.ads.helper.JsonHelper
import com.lazygeniouz.house.ads.listener.NativeAdListener
import com.lazygeniouz.house.ads.modal.DialogModal
import com.lazygeniouz.house.ads.modal.HouseAdsNativeView
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("unused")
class HouseAdsNative(context: Context, private val jsonUrl: String) : BaseAd(context) {

    private var isUsingRawRes = false
    private var jsonRawResponse = ""
    private var jsonLocalRawResponse = ""

    private var isAdLoaded = false
    private var lastLoaded = 0

    private var usePalette = true
    private var hideIfAppInstalled = false

    private var customNativeView: View? = null
    private var nativeAdView: HouseAdsNativeView? = null
    private var mNativeAdListener: NativeAdListener? = null
    private var callToActionListener: NativeAdListener.CallToActionListener? = null

    private val jsonHelper: JsonHelper = JsonHelper()

    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = jsonHelper.getJsonFromRaw(context, rawFile)
    }

    fun setNativeAdView(nativeAdView: HouseAdsNativeView): HouseAdsNative {
        this.nativeAdView = nativeAdView
        return this
    }

    fun setNativeAdView(view: View): HouseAdsNative {
        this.customNativeView = view
        return this
    }

    fun hideIfAppInstalled(hide: Boolean): HouseAdsNative {
        this.hideIfAppInstalled = hide
        return this
    }

    fun usePalette(usePalette: Boolean): HouseAdsNative {
        this.usePalette = usePalette
        return this
    }

    fun setNativeAdListener(listener: NativeAdListener): HouseAdsNative {
        this.mNativeAdListener = listener
        return this
    }

    fun setCallToActionListener(listener: NativeAdListener.CallToActionListener): HouseAdsNative {
        this.callToActionListener = listener
        return this
    }

    fun loadAds() {
        isAdLoaded = false
        if (!isUsingRawRes) {
            require(jsonUrl.trim().isNotEmpty()) { context.getString(R.string.error_url_blank) }
            if (jsonRawResponse.isEmpty()) {
                launch {
                    val result = jsonHelper.getJsonObject(jsonUrl)
                    if (result.trim().isNotEmpty()) {
                        jsonRawResponse = result
                        configureAds(result)
                    } else mNativeAdListener?.onAdLoadFailed(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

    private fun configureAds(response: String) = launch {
        val modalList = ArrayList<DialogModal>()

        try {
            val rootObject = JSONObject(response)
            val array = rootObject.optJSONArray("apps")!!

            for (childObject in 0 until array.length()) {
                val jsonObject = array.getJSONObject(childObject)
                if (hideIfAppInstalled && !jsonObject.optString("app_uri").hasHttpSign &&
                        context.isAppInstalled(jsonObject.optString("app_uri"))) {
                    array.remove(childObject)
                } else {
                    //We Only Add Native Ones!
                    if (jsonObject.optString("app_adType") == "native") {
                        val dialogModal = DialogModal()
                        dialogModal.appTitle = jsonObject.optString("app_title")
                        dialogModal.appDesc = jsonObject.optString("app_desc")
                        dialogModal.iconUrl = jsonObject.optString("app_icon")
                        dialogModal.largeImageUrl = jsonObject.optString("app_header_image")
                        dialogModal.callToActionButtonText = jsonObject.optString("app_cta_text")
                        dialogModal.packageOrUrl = jsonObject.optString("app_uri")
                        dialogModal.setRating(jsonObject.optString("app_rating"))
                        dialogModal.price = jsonObject.optString("app_price")

                        modalList.add(dialogModal)
                    }
                }
            }

        } catch (jsonException: JSONException) {
            jsonException.printStackTrace()
        }

        if (modalList.size > 0) {
            val dialogModal = modalList[lastLoaded]
            if (lastLoaded == modalList.size - 1)
                lastLoaded = 0
            else lastLoaded++

            val title: TextView?
            val description: TextView?
            val price: TextView?
            val callToActionView: View?
            val icon: ImageView?
            val headerImage: ImageView?
            val ratings: RatingBar?


            if (nativeAdView != null) {
                val view = nativeAdView
                title = view!!.titleView
                description = view.descriptionView
                price = view.priceView
                callToActionView = view.callToActionView
                icon = view.iconView
                headerImage = view.headerImageView
                ratings = view.ratingsView
            } else {
                if (customNativeView != null) {
                    title = customNativeView!!.findViewById(R.id.houseAds_title)
                    description = customNativeView!!.findViewById(R.id.houseAds_description)
                    price = customNativeView!!.findViewById(R.id.houseAds_price)
                    callToActionView = customNativeView!!.findViewById(R.id.houseAds_cta)
                    icon = customNativeView!!.findViewById(R.id.houseAds_app_icon)
                    headerImage = customNativeView!!.findViewById(R.id.houseAds_header_image)
                    ratings = customNativeView!!.findViewById(R.id.houseAds_rating)
                } else throw NullPointerException(context.getString(R.string.error_native_ad_null))
            }

            val iconUrl = dialogModal.iconUrl!!
            val largeImageUrl = dialogModal.largeImageUrl!!

            if (!isUsingRawRes) {
                require(!(iconUrl.trim().isEmpty() || !iconUrl.trim().hasHttpSign)) { context.getString(R.string.error_icon_url_null) }
                require(!(largeImageUrl.trim().isNotEmpty() && !largeImageUrl.trim().hasHttpSign)) { context.getString(R.string.error_header_image_url_null) }
                require(!(dialogModal.appTitle!!.trim().isEmpty() || dialogModal.appDesc!!.trim().isEmpty())) { context.getString(R.string.error_title_description_null) }
            } else {
                if (iconUrl.trim().isNotEmpty()) {
                    when {
                        iconUrl.trim().startsWith("http") -> Log.d(TAG, "App Logo param starts with `http://`")
                        iconUrl.trim().startsWith("@drawable/") -> Log.d(TAG, "App Logo param is a local drawable")
                        else -> throw IllegalArgumentException(context.getString(R.string.error_raw_resource_icon_null))
                    }
                }
                if (largeImageUrl.trim().isNotEmpty()) {
                    when {
                        largeImageUrl.trim().startsWith("http") -> Log.d(TAG, "App Header param starts with `http://`")
                        largeImageUrl.trim().startsWith("@drawable/") -> Log.d(TAG, "App Header param is a local drawable")
                        else -> throw IllegalArgumentException(context.getString(R.string.error_raw_resource_header_image_null))
                    }
                }
            }

            val iconUrlToLoad: String = if (iconUrl.hasDrawableSign) context.getDrawableUriAsString(iconUrl)!!
            else iconUrl


            when (val result = getImageFromNetwork(iconUrlToLoad)) {
                is SuccessResult -> {
                    if (usePalette) {
                        val bitmap = (result.drawable as BitmapDrawable).bitmap
                        icon!!.setImageBitmap(bitmap)
                        val palette = Palette.from(bitmap).generate()
                        val dominantColor = palette.getDominantColor(ContextCompat.getColor(context, R.color.colorAccent))

                        val drawable = GradientDrawable()
                        drawable.setColor(dominantColor)
                        callToActionView!!.background = drawable

                        if (dialogModal.getRating() > 0) {
                            ratings!!.rating = dialogModal.getRating()
                            val ratingsDrawable = ratings.progressDrawable
                            DrawableCompat.setTint(ratingsDrawable, dominantColor)
                        } else
                            ratings!!.visibility = View.GONE
                    }


                    if (largeImageUrl.trim().isEmpty()) {
                        isAdLoaded = true
                        mNativeAdListener?.onAdLoaded()
                    }

                }
                is ErrorResult -> {
                    isAdLoaded = false
                    if (headerImage == null || dialogModal.largeImageUrl!!.isEmpty()) {
                        mNativeAdListener?.onAdLoadFailed(Exception(result.throwable))
                    }
                }
            }

            if (largeImageUrl.trim().isNotEmpty()) {
                val largeImageUrlToLoad: String = if (largeImageUrl.hasDrawableSign) context.getDrawableUriAsString(largeImageUrl)!!
                else largeImageUrl

                when (val result = getImageFromNetwork(largeImageUrlToLoad)) {
                    is SuccessResult -> {
                        val bitmap = (result.drawable as BitmapDrawable).bitmap
                        if (headerImage != null) {
                            headerImage.setImageBitmap(bitmap)
                            headerImage.visibility = View.VISIBLE
                        }
                        isAdLoaded = true
                        mNativeAdListener?.onAdLoaded()
                    }
                    is ErrorResult -> {
                        mNativeAdListener?.onAdLoadFailed(Exception(result.throwable))
                        isAdLoaded = false
                    }
                }
            } else headerImage?.visibility = View.GONE

            title!!.text = dialogModal.appTitle
            description!!.text = dialogModal.appDesc
            if (price != null) {
                price.visibility = View.VISIBLE
                if (dialogModal.price!!.trim().isNotEmpty())
                    price.text = String.format(context.getString(R.string.price_format), dialogModal.price)
                else
                    price.visibility = View.GONE
            }

            if (ratings != null) {
                ratings.visibility = View.VISIBLE
                if (dialogModal.getRating() > 0) ratings.rating = dialogModal.getRating()
                else ratings.visibility = View.GONE
            }

            if (callToActionView != null) {
                if (callToActionView is TextView) callToActionView.text = dialogModal.callToActionButtonText
                if (callToActionView is Button) callToActionView.text = dialogModal.callToActionButtonText
                require(callToActionView is TextView) { context.getString(R.string.error_cta_is_not_textview_instance) }

                callToActionView.setOnClickListener { view ->
                    if (callToActionListener != null)
                        callToActionListener!!.onCallToActionClicked(view)
                    else {
                        val packageOrUrl = dialogModal.packageOrUrl
                        if (packageOrUrl!!.trim().startsWith("http")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)))
                        } else {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageOrUrl")))
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageOrUrl")))
                            }

                        }
                    }
                }
            }
        }
    }

    private companion object {
        private val TAG = HouseAdsNative::class.java.simpleName
    }
}
