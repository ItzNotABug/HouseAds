package com.lazygeniouz.house.ads.extension

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.AnyRes
import coil.ImageLoader

val String.hasHttpSign: Boolean
    get() {
        return this.startsWith("http")
    }

val String.hasDrawableSign: Boolean
    get() {
        return this.startsWith("@drawable/")
    }

fun Context.isAppInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.getDrawableUriAsString(name: String): String? {
    val drawableName = name.substringAfterLast("/")
    val resourceId = resources.getIdentifier(drawableName, "drawable", packageName)
    return getUriToResource(resourceId).toString()
}

private fun Context.getUriToResource(@AnyRes resId: Int): Uri {
    return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" + resources.getResourcePackageName(resId)
            + '/'.toString() + resources.getResourceTypeName(resId)
            + '/'.toString() + resources.getResourceEntryName(resId))
}