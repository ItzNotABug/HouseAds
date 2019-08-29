/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.helper

import android.os.AsyncTask
import android.util.Log
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class JsonPullerTask(private val jsonUrl: String, private val listener: JsonPullerListener) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg p1: String): String {
        return HouseAdsHelper.parseJsonObject(jsonUrl)
    }

    override fun onPostExecute(result: String) {
        listener.onPostExecute(result)
        Log.d("Response", result)
    }

    interface JsonPullerListener {
        fun onPostExecute(result: String)
    }
}
