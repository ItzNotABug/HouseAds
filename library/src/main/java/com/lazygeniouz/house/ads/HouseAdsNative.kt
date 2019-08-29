package com.lazygeniouz.house.ads

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.palette.graphics.Palette
import com.lazygeniouz.house.ads.helper.HouseAdsHelper
import com.lazygeniouz.house.ads.helper.JsonPullerTask
import com.lazygeniouz.house.ads.helper.RemoveJsonObjectCompat
import com.lazygeniouz.house.ads.listener.NativeAdListener
import com.lazygeniouz.house.ads.modal.DialogModal
import com.lazygeniouz.house.ads.modal.HouseAdsNativeView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("unused")
class HouseAdsNative(private val mContext: Context, private val jsonUrl: String) {

    var isAdLoaded = false
    private var usePalette = true
    private var hideIfAppInstalled = false

    private var customNativeView: View? = null
    private var nativeAdView: HouseAdsNativeView? = null
    private var mNativeAdListener: NativeAdListener? = null
    private var callToActionListener: NativeAdListener.CallToActionListener? = null

    fun setNativeAdView(nativeAdView: HouseAdsNativeView) {
        this.nativeAdView = nativeAdView
    }

    fun setNativeAdView(view: View) {
        this.customNativeView = view
    }

    fun hideIfAppInstalled(hide: Boolean) {
        this.hideIfAppInstalled = hide
    }

    fun usePalette(usePalette: Boolean) {
        this.usePalette = usePalette
    }

    fun setNativeAdListener(listener: NativeAdListener) {
        this.mNativeAdListener = listener
    }

    fun setCallToActionListener(listener: NativeAdListener.CallToActionListener) {
        this.callToActionListener = listener
    }

    fun loadAds() {
        isAdLoaded = false
        require(jsonUrl.trim { it <= ' ' }.isNotEmpty()) { "Url is Blank!" }
        JsonPullerTask(jsonUrl, object : JsonPullerTask.JsonPullerListener {
            override fun onPostExecute(result: String) {
                if (result.trim { it <= ' ' }.isNotEmpty()) setUp(result)
                else {
                    mNativeAdListener?.onAdLoadFailed(Exception("Null Response"))
                }
            }

        }).execute()
    }

    private fun setUp(response: String) {
        val modalList = ArrayList<DialogModal>()

        try {
            val rootObject = JSONObject(response)
            val array = rootObject.optJSONArray("apps")

            for (childObject in 0 until array.length()) {
                val jsonObject = array.getJSONObject(childObject)
                if (hideIfAppInstalled && !jsonObject.optString("app_uri").startsWith("http") && HouseAdsHelper.isAppInstalled(mContext, jsonObject.optString("app_uri"))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) array.remove(childObject)
                    else RemoveJsonObjectCompat(childObject, array).execute()
                }
                else {
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
                } else throw NullPointerException("NativeAdView is Null. Either pass HouseAdsNativeView or a View object in `setNativeAdView()`")
            }
            require(!(dialogModal.iconUrl!!.trim { it <= ' ' }.isEmpty() || !dialogModal.iconUrl!!.trim { it <= ' ' }.contains("http"))) { "Icon URL should not be Null or Blank & should start with \"http\"" }
            require(!(dialogModal.largeImageUrl!!.trim { it <= ' ' }.isNotEmpty() && !dialogModal.largeImageUrl!!.trim { it <= ' ' }.contains("http"))) { "Header Image URL should start with \"http\"" }
            require(!(dialogModal.appTitle!!.trim { it <= ' ' }.isEmpty() || dialogModal.appDesc!!.trim { it <= ' ' }.isEmpty())) { "Title & description should not be Null or Blank." }

            Picasso.get().load(dialogModal.iconUrl).into(icon!!, object : Callback {
                override fun onSuccess() {
                    if (usePalette) {
                        val palette = Palette.from((icon.drawable as BitmapDrawable).bitmap).generate()
                        val dominantColor = palette.getDominantColor(ContextCompat.getColor(mContext, R.color.colorAccent))

                        if (callToActionView!!.background is ColorDrawable) callToActionView.background = GradientDrawable()
                        val drawable = callToActionView.background as GradientDrawable
                        drawable.setColor(dominantColor)

                        if (dialogModal.getRating() > 0) {
                            ratings!!.rating = dialogModal.getRating()
                            val ratingsDrawable = ratings.progressDrawable
                            DrawableCompat.setTint(ratingsDrawable, dominantColor)
                        } else
                            ratings!!.visibility = View.GONE
                    }


                    if (dialogModal.largeImageUrl!!.trim { it <= ' ' }.isEmpty()) {
                        isAdLoaded = true
                        mNativeAdListener?.onAdLoaded()
                    }
                }

                override fun onError(exception: Exception) {
                    isAdLoaded = false
                    if (headerImage == null || dialogModal.largeImageUrl!!.isEmpty()) {
                        mNativeAdListener?.onAdLoadFailed(exception)
                    }
                }
            })


            if (dialogModal.largeImageUrl!!.trim { it <= ' ' }.isNotEmpty())
                Picasso.get().load(dialogModal.largeImageUrl).into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        if (headerImage != null) {
                            headerImage.visibility = View.VISIBLE
                            headerImage.setImageBitmap(bitmap)
                        }
                        isAdLoaded = true
                        mNativeAdListener?.onAdLoaded()
                    }

                    override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable?) {
                        mNativeAdListener?.onAdLoadFailed(exception)
                        isAdLoaded = false
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
            else headerImage?.visibility = View.GONE

            title!!.text = dialogModal.appTitle
            description!!.text = dialogModal.appDesc
            if (price != null) {
                price.visibility = View.VISIBLE
                if (dialogModal.price!!.trim { it <= ' ' }.isNotEmpty())
                    price.text = String.format("Price: %s", dialogModal.price)
                else
                    price.visibility = View.GONE
            }

            if (ratings != null) {
                ratings.visibility = View.VISIBLE
                if (dialogModal.getRating() > 0)
                    ratings.rating = dialogModal.getRating()
                else
                    ratings.visibility = View.GONE
            }

            if (callToActionView != null) {
                if (callToActionView is TextView) callToActionView.text = dialogModal.callToActionButtonText
                if (callToActionView is Button) callToActionView.text = dialogModal.callToActionButtonText
                require(callToActionView is TextView) { "Call to Action View must be either a Button or a TextView" }

                callToActionView.setOnClickListener { view ->
                    if (callToActionListener != null)
                        callToActionListener!!.onCallToActionClicked(view)
                    else {
                        val packageOrUrl = dialogModal.packageOrUrl
                        if (packageOrUrl!!.trim { it <= ' ' }.startsWith("http")) {
                            mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)))
                        } else {
                            try {
                                mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageOrUrl")))
                            } catch (e: ActivityNotFoundException) {
                                mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageOrUrl")))
                            }

                        }
                    }
                }
            }
        }
    }

    companion object {
        private var lastLoaded = 0
    }
}
