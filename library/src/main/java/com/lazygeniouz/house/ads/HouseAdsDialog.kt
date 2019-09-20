package com.lazygeniouz.house.ads

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
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
import com.lazygeniouz.house.ads.extension.hasDrawableSign
import com.lazygeniouz.house.ads.extension.hasHttpSign
import com.lazygeniouz.house.ads.helper.HouseAdsHelper
import com.lazygeniouz.house.ads.helper.HouseAdsHelper.getDrawableUriAsString
import com.lazygeniouz.house.ads.helper.JsonPullerTask
import com.lazygeniouz.house.ads.helper.RemoveJsonObjectCompat
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.modal.DialogModal
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HouseAdsDialog(private val context: Context, private val jsonUrl: String) {

    private var jsonRawResponse = ""
    private var jsonLocalRawResponse = ""
    private var showHeader = true
    private var hideIfAppInstalled = true
    private var usePalette = true
    private var cardCorner = 25
    private var callToActionButtonCorner = 25

    private var lastLoaded = 0
    private var isAdLoaded = false
    private var isUsingRawRes = false

    private var mAdListener: AdListener? = null
    private var dialog: AlertDialog? = null

    constructor(context: Context, @RawRes rawFile: Int) : this(context, "") {
        isUsingRawRes = true
        jsonLocalRawResponse = HouseAdsHelper.getJsonFromRaw(context, rawFile)
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

    fun loadAds(): HouseAdsDialog {
        isAdLoaded = false
        if (!isUsingRawRes) {
            require(jsonUrl.trim().isNotEmpty()) { context.getString(R.string.error_url_blank) }
            if (jsonRawResponse.isEmpty()) {
                JsonPullerTask(jsonUrl, object : JsonPullerTask.JsonPullerListener {
                    override fun onPostExecute(result: String) {
                        if (result.trim().isNotEmpty()) {
                            jsonRawResponse = result
                            configureAds(result)
                        }
                        else {
                            mAdListener?.onAdLoadFailed(Exception(context.getString(R.string.error_null_response)))
                        }
                    }

                }).execute()
            }
            else configureAds(jsonRawResponse)
        }
        else configureAds(jsonLocalRawResponse)
        return this
    }

    fun showAd(): HouseAdsDialog {
        if (dialog != null) dialog!!.show()
        return this
    }

    private fun configureAds(response: String) {
        val builder = AlertDialog.Builder(context)
        val dialogModalList = ArrayList<DialogModal>()

        try {
            val rootObject = JSONObject(response)
            val jsonArray = rootObject.optJSONArray("apps")

            for (childObject in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(childObject)

                if (hideIfAppInstalled && !jsonObject.optString("app_uri").hasHttpSign && HouseAdsHelper.isAppInstalled(context, jsonObject.optString("app_uri"))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) jsonArray.remove(childObject)
                    else RemoveJsonObjectCompat(childObject, jsonArray).execute()
                } else {
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
            if (lastLoaded == dialogModalList.size - 1) lastLoaded = 0
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
            }
            else {
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

            val iconUrlToLoad: String = if (iconUrl.hasDrawableSign) getDrawableUriAsString(context, iconUrl)!!
            else iconUrl

            Picasso.get().load(iconUrlToLoad).into(icon, object : Callback {
                override fun onSuccess() {
                    isAdLoaded = true
                    mAdListener?.onAdLoaded()

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
                    }
                    else ratings.visibility = View.GONE
                }

                override fun onError(exception: Exception) {
                    isAdLoaded = false
                    mAdListener?.onAdLoadFailed(exception)
                    icon.visibility = View.GONE
                }
            })

            if (largeImageUrl.trim().isNotEmpty() && showHeader) {
                val largeImageUrlToLoad: String = if (largeImageUrl.hasDrawableSign) getDrawableUriAsString(context, largeImageUrl)!!
                else largeImageUrl

                Picasso.get().load(largeImageUrlToLoad).into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        headerImage.setImageBitmap(bitmap)
                        headerImage.visibility = View.VISIBLE
                    }

                    override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable?) {
                        headerImage.visibility = View.GONE
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
            }
            else headerImage.visibility = View.GONE

            title.text = dialogModal.appTitle
            description.text = dialogModal.appDesc
            callToActionButton.text = dialogModal.callToActionButtonText
            if (dialogModal.price!!.trim().isEmpty()) price.visibility = View.GONE
            else price.text = String.format(context.getString(R.string.price_format), dialogModal.price)


            builder.setView(view)
            dialog = builder.create()
            dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog!!.setOnShowListener { mAdListener?.onAdShown() }
            dialog!!.setOnCancelListener { mAdListener?.onAdClosed() }
            dialog!!.setOnDismissListener { mAdListener?.onAdClosed() }

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
    


    private companion object {
        private val TAG = HouseAdsDialog::class.java.simpleName.toString()
    }
}




