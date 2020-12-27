/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.helper

import android.content.Context
import android.util.Log
import androidx.annotation.RestrictTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object JsonHelper {

    internal suspend fun getJsonObject(url: String)
            : String = withContext(Dispatchers.IO) {
        return@withContext parseJsonObject(url)
    }

    private fun parseJsonObject(url: String): String {
        var document: Document? = null
        try {
            document = Jsoup
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
        } catch (error: IOException) {
            Log.e("HouseAds", "${error.message}")
            error.printStackTrace()
        }


        return document?.body()?.text() ?: ""
    }

    internal fun getJsonFromRaw(ctx: Context, resId: Int): String {
        val inputStream = ctx.resources.openRawResource(resId)
        val inputReader = InputStreamReader(inputStream)
        val buffReader = BufferedReader(inputReader)
        var line: String? = null
        val text = StringBuilder()

        try {
            while ({ line = buffReader.readLine(); line }() != null) {
                text.append(line)
                text.append('\n')
            }
        } catch (e: IOException) {
            text.append("")
        }

        return text.toString()
    }
}
