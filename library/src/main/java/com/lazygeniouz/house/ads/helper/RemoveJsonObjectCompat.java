/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.helper;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RemoveJsonObjectCompat extends AsyncTask<JSONArray, JSONArray, JSONArray> {
    private JSONArray jsonArray;
    private int index;

    public RemoveJsonObjectCompat(int index, JSONArray array) {
        this.jsonArray = array;
        this.index = index;
    }


    @Override
    protected JSONArray doInBackground(JSONArray... jsonArrays) {
        return removeJsonObject(index, jsonArray);
    }


    private JSONArray removeJsonObject(final int idx, final JSONArray from) {
        final List<JSONObject> objects = asList(from);
        objects.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objects) {
            ja.put(obj);
        }

        return ja;
    }

    private List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

}