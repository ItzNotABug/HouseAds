/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018-2019.
 */

package com.lazygeniouz.house.ads.helper

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

import androidx.annotation.RestrictTo

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object HouseAdsHelper {
    internal fun parseJsonObject(url: String): String {
        var doc: Document? = null
        try {
            doc = Jsoup
                    .connect(url.trim { it <= ' ' })
                    .ignoreContentType(true)
                    .timeout(3000)
                    .header("Connection", "keep-alive")
                    .header("Cache-Control", "max-age=0")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "HouseAds (App)")
                    .header("Accept-Encoding", "gzip,deflate,sdch")
                    .header("Accept-Language", "en-US,en;q=0.8,ru;q=0.6")
                    .get()
        } catch (e: IOException) {
            Log.e("HouseAds", e.message)
            e.printStackTrace()
        }


        return if (doc != null)
            doc.body().text()
        else
            ""
    }

    fun isAppInstalled(mActivity: Context, packageName: String): Boolean {
        return try {
            mActivity.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
