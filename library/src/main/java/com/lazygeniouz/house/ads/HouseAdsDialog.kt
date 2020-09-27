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
import androidx.palette.graphics.Palette
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.lazygeniouz.house.ads.base.BaseAd
import com.lazygeniouz.house.ads.extension.getDrawableUriAsString
import com.lazygeniouz.house.ads.extension.hasDrawableSign
import com.lazygeniouz.house.ads.extension.hasHttpSign
import com.lazygeniouz.house.ads.extension.isAppInstalled
import com.lazygeniouz.house.ads.helper.JsonHelper
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.modal.DialogModal
import org.json.JSONException
import org.json.JSONObject
import java.util.*

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

    private val jsonHelper: JsonHelper = JsonHelper()
    private var mAdListener: AdListener? = null
    private var dialog: AlertDialog? = null

    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = jsonHelper.getJsonFromRaw(context, rawFile)
    }

    fun showHeaderIfAvailable(showHeader: Boolean): HouseAdsDialog {
        this.showHeader = showHeader
        return this
    }

    fun setCardCorners(corners: Int): HouseAdsDialog {
        this.cardCorner = corners
        return this
    }

    fun setCtaCorner(corner: Int): HouseAdsDialog {
        this.callToActionButtonCorner = corner
        return this
    }

    fun ctaAllCaps(isAllCaps: Boolean): HouseAdsDialog {
        this.isAllCaps = isAllCaps
        return this
    }

    fun setAdListener(listener: AdListener): HouseAdsDialog {
        this.mAdListener = listener
        return this
    }

    @Suppress("unused")
    fun isAdLoaded(): Boolean {
        return isAdLoaded
    }

    fun hideIfAppInstalled(hide: Boolean): HouseAdsDialog {
        this.hideIfAppInstalled = hide
        return this
    }

    fun usePalette(usePalette: Boolean): HouseAdsDialog {
        this.usePalette = usePalette
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
                    } else mAdListener?.onAdLoadFailed(Exception(context.getString(R.string.error_null_response)))
                }
            } else configureAds(jsonRawResponse)
        } else configureAds(jsonLocalRawResponse)
    }

    fun showAd() {
        if (dialog == null) Log.d(TAG, "dialog is null")
        dialog?.show()
    }

    private fun configureAds(response: String) = launch {
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
                    //We Only Add Dialog Ones!
                    if (jsonObject.optString("app_adType") == "dialog") {
                        val dialogModal = DialogModal()
                        dialogModal.appTitle = jsonObject.optString("app_title")
                        dialogModal.appDesc = jsonObject.optString("app_desc")
                        dialogModal.iconUrl = jsonObject.optString("app_icon")
                        dialogModal.largeImageUrl = jsonObject.optString("app_header_image")
                        dialogModal.callToActionButtonText = jsonObject.optString("app_cta_text")
                        dialogModal.packageOrUrl = jsonObject.optString("app_uri")
                        dialogModal.setRating(jsonObject.optString("app_rating"))
                        dialogModal.price = jsonObject.optString("app_price")

                        dialogModalList.add(dialogModal)
                    }
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

            val iconUrlToLoad: String = if (iconUrl.hasDrawableSign) context.getDrawableUriAsString(iconUrl)!!
            else iconUrl
            when (val result = getImageFromNetwork(iconUrlToLoad)) {
                is SuccessResult -> {
                    icon.setImageDrawable(result.drawable)
                    isAdLoaded = true

                    if (icon.visibility == View.GONE) icon.visibility = View.VISIBLE
                    var dominantColor = ContextCompat.getColor(context, R.color.colorAccent)
                    if (usePalette) {
                        val palette = Palette.from((icon.drawable as BitmapDrawable).bitmap).generate()
                        dominantColor = palette.getDominantColor(ContextCompat.getColor(context, R.color.colorAccent))
                    }

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
                    mAdListener?.onAdLoadFailed(Exception("The Icon Uri: $iconUrlToLoad could not be fetched. More Info: ${result.throwable}"))
                    icon.visibility = View.GONE
                }
            }

            if (largeImageUrl.trim().isNotEmpty() && showHeader) {
                val largeImageUrlToLoad: String = if (largeImageUrl.hasDrawableSign) context.getDrawableUriAsString(largeImageUrl)!!
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
            dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog!!.setOnShowListener { mAdListener?.onAdShown() }
            dialog!!.setOnCancelListener { mAdListener?.onAdClosed() }
            dialog!!.setOnDismissListener { mAdListener?.onAdClosed() }

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
                        mAdListener?.onApplicationLeft()
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageOrUrl")))
                    }

                }
            }
        }
    }

    companion object {
        private val TAG = HouseAdsInterstitial::class.java.simpleName
    }
}




