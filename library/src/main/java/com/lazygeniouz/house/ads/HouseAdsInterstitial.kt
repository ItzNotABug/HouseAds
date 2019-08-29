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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import com.lazygeniouz.house.ads.helper.JsonPullerTask
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.modal.InterstitialModal
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("unused")
class HouseAdsInterstitial(private val context: Context, private val jsonUrl: String) {
    
    private var lastLoaded = 0
    fun setAdListener(adListener: AdListener) {
        mAdListener = adListener
    }

    fun loadAd() {
        require(jsonUrl.trim { it <= ' ' }.isNotEmpty()) { "Url is Blank!" }
        JsonPullerTask(jsonUrl, object : JsonPullerTask.JsonPullerListener {
            override fun onPostExecute(result: String) {
                if (result.trim { it <= ' ' }.isNotEmpty()) setUp(result)
                else {
                    mAdListener?.onAdLoadFailed(Exception("Null Response"))
                }
            }

        }).execute()
    }

    fun isAdLoaded(): Boolean {
        return isAdLoaded
    }

    private fun setUp(jsonResponse: String) {
        val modalArrayList = ArrayList<InterstitialModal>()
        val appendedString = String(StringBuilder().append(jsonResponse))

        try {
            val rootObject = JSONObject(appendedString)
            val array = rootObject.optJSONArray("apps")

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

        if (modalArrayList.size > 0) {
            val modal = modalArrayList[lastLoaded]
            if (lastLoaded == modalArrayList.size - 1)
                lastLoaded = 0
            else
                lastLoaded++

            require(!(modal.interstitialImageUrl.isEmpty() || !modal.interstitialImageUrl.startsWith("http"))) { "Interstitial Image URL should not be Null or Blank & should start with 'http'" }

            Picasso.get().load(modal.interstitialImageUrl).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(resource: Bitmap, from: Picasso.LoadedFrom) {
                    bitmap = resource
                    mAdListener?.onAdLoaded()
                    isAdLoaded = true
                }

                override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable?) {
                    mAdListener?.onAdLoadFailed(exception)
                    isAdLoaded = false
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
            packageName = modal.packageOrUrl
        }
    }

    fun show() {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(0, 0)
    }


    fun show(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
        context.startActivity(Intent(context, InterstitialActivity::class.java))
        if (context is AppCompatActivity) context.overridePendingTransition(enterAnim, exitAnim)
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
                if (packageName!!.startsWith("http")) {
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

    companion object {
        private var mAdListener: AdListener? = null
        private var isAdLoaded = false
        private var bitmap: Bitmap? = null
        private var packageName: String? = null
    }
}
