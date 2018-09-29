/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;

public class HouseAdsGlideModule implements GlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        //noinspection ConstantConditions
        String path = context.getExternalCacheDir().getAbsolutePath() + "/glideCache/";
        int cacheSize = 104857600;

        builder.setDiskCache(new DiskLruCacheFactory(path, cacheSize));
    }

    @Override
    public void registerComponents(Context context, Glide glide) { }
}
