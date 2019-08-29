/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.helper

import android.os.AsyncTask
import androidx.annotation.RestrictTo
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/*
`JsonArray.remove(index)` is only available on API 19+,
so this utility class helps us do that dirty work on pre 19.
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RemoveJsonObjectCompat(private val index: Int, private val jsonArray: JSONArray) : AsyncTask<JSONArray, JSONArray, JSONArray>() {

    override fun doInBackground(vararg jsonArrays: JSONArray): JSONArray {
        return removeJsonObject(index, jsonArray)
    }

    private fun removeJsonObject(child: Int, array: JSONArray): JSONArray {
        val objects = asList(array)
        objects.removeAt(child)

        val jsonArray = JSONArray()
        for (obj in objects) jsonArray.put(obj)
        return jsonArray
    }

    private fun asList(jsonArray: JSONArray): MutableList<JSONObject> {
        val length = jsonArray.length()
        val result = ArrayList<JSONObject>(length)
        for (index in 0 until length) {
            val element = jsonArray.optJSONObject(index)
            if (element != null) result.add(element)
        }
        return result
    }
}