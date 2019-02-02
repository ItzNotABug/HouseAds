/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class HouseAdsHelper {
    static String parseJsonObject(String url) {
        Document doc = null;
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
                    .get();
        } catch (IOException e) {
            Log.e("HouseAds", e.getMessage());
            e.printStackTrace();
        }


        if (doc != null) return doc.body().text();
        else return "";
    }

    public static boolean isAppInstalled(Context mActivity, String packageName) {
        PackageManager pm = mActivity.getPackageManager();
        boolean isInstalled;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }
        return isInstalled;
    }
}
