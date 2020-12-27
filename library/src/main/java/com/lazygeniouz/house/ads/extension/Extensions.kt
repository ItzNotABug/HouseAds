/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.extension

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.annotation.AnyRes
import androidx.palette.graphics.Palette
import com.lazygeniouz.house.ads.modal.DialogModal
import com.lazygeniouz.house.ads.modal.InterstitialModal
import org.json.JSONObject

internal val String.hasHttpSign: Boolean
    get() {
        return this.startsWith("http")
    }

internal val String.hasDrawableSign: Boolean
    get() {
        return this.startsWith("@drawable/")
    }

internal fun Context.isAppInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

internal fun Context.getDrawableUriAsString(name: String): String {
    val drawableName = name.substringAfterLast("/")
    val resourceId = resources.getIdentifier(drawableName, "drawable", packageName)
    return getUriToResource(resourceId).toString()
}

internal fun Bitmap.getDominantColorForInterstitial(): Int {
    val newBitmap = Bitmap.createScaledBitmap(this, 1, 1, true)
    val color = newBitmap.getPixel(0, 0)
    newBitmap.recycle()
    return color
}

internal fun Bitmap.getDominantColor(): Int {
    val palette = Palette.from(this).generate()
    return palette.getDominantColor(Color.parseColor("#ff4081"))
}

private fun Context.getUriToResource(@AnyRes resId: Int): Uri {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" + resources.getResourcePackageName(resId)
            + '/'.toString() + resources.getResourceTypeName(resId)
            + '/'.toString() + resources.getResourceEntryName(resId))
}

internal fun getDialogModal(jsonObject: JSONObject): DialogModal {
    return DialogModal()
            .apply {
                appTitle = jsonObject.optString("app_title")
                appDesc = jsonObject.optString("app_desc")
                iconUrl = jsonObject.optString("app_icon")
                largeImageUrl = jsonObject.optString("app_header_image")
                callToActionButtonText = jsonObject.optString("app_cta_text")
                packageOrUrl = jsonObject.optString("app_uri")
                rating = jsonObject.optString("app_rating")
                price = jsonObject.optString("app_price")
            }
}

internal fun getInterstitialModal(jsonObject: JSONObject) =
        InterstitialModal(
                jsonObject.optString("app_interstitial_url"),
                jsonObject.optString("app_uri"))