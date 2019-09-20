/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018-2019.
 */

package com.lazygeniouz.house.ads.helper

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.annotation.AnyRes
import androidx.annotation.RestrictTo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object HouseAdsHelper {
    internal fun parseJsonObject(url: String): String {
        var doc: Document? = null
        try {
            doc = Jsoup
                    .connect(url.trim())
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

    internal fun isAppInstalled(mActivity: Context, packageName: String): Boolean {
        return try {
            mActivity.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    internal fun getJsonFromRaw(ctx: Context, resId: Int): String {
        val inputStream = ctx.resources.openRawResource(resId)
        val inputReader = InputStreamReader(inputStream)
        val buffReader = BufferedReader(inputReader)
        var line: String? = null
        val text = StringBuilder()

        try {
            while ({line = buffReader.readLine(); line}() != null) {
                text.append(line)
                text.append('\n')
            }
        } catch (e: IOException) {
            text.append("")
        }

        return text.toString()
    }

    internal fun getDrawableUriAsString(context: Context, name: String): String? {
        val drawableName = name.substringAfterLast("/")
        val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        return getUriToResource(context, resourceId).toString()
    }

    private fun getUriToResource(context: Context, @AnyRes resId: Int): Uri {
        val res = context.resources
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/'.toString() + res.getResourceTypeName(resId)
                + '/'.toString() + res.getResourceEntryName(resId))
    }
}
