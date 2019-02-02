/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lazygeniouz.house.ads.helper.JsonPullerTask;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.modal.InterstitialModal;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.AnimRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HouseAdsInterstitial {
    private final Context mContext;
    private static AdListener mAdListener;
    private String url;
    private int lastLoaded = 0;

    private static boolean isAdLoaded = false;
    private static Bitmap bitmap;
    private static String packageName;

    public HouseAdsInterstitial(Context context, String url) {
        this.mContext = context;
        this.url = url;
    }

    public void setAdListener(AdListener adListener) {
        mAdListener = adListener;
    }

    public void loadAd() {
        if (url.trim().isEmpty()) throw new IllegalArgumentException("Url is Blank!");
        else {
            new JsonPullerTask(url, result -> {
                if (!result.trim().isEmpty()) setUp(result);
                else {
                    if (mAdListener != null) mAdListener.onAdLoadFailed(new Exception("Null Response"));
                }
            }).execute();
        }
    }

    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    @SuppressLint("CheckResult")
    private void setUp(String val) {
        ArrayList<InterstitialModal> modalArrayList = new ArrayList<>();
        String x = new String(new StringBuilder().append(val));

        try {
            JSONObject rootObject = new JSONObject(x);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                JSONObject jsonObject = array.getJSONObject(object);

                if (jsonObject.optString("app_adType").equals("interstitial")) {
                    InterstitialModal interstitialModal = new InterstitialModal();
                    interstitialModal.setInterstitialImageUrl(jsonObject.optString("app_interstitial_url"));
                    interstitialModal.setPackageOrUrl(jsonObject.optString("app_uri"));
                    modalArrayList.add(interstitialModal);
                }
            }

        } catch (JSONException e) { e.printStackTrace(); }

        if (modalArrayList.size() > 0) {
            final InterstitialModal modal = modalArrayList.get(lastLoaded);
            if (lastLoaded == modalArrayList.size() - 1) lastLoaded = 0;
            else lastLoaded++;

            Picasso.get().load(modal.getInterstitialImageUrl()).into(new com.squareup.picasso.Target() {
                @Override
                public void onBitmapLoaded(Bitmap resource, Picasso.LoadedFrom from) {
                    bitmap = resource;
                    if (mAdListener != null) mAdListener.onAdLoaded();
                    isAdLoaded = true;
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    if (mAdListener != null) mAdListener.onAdLoadFailed(e);
                    isAdLoaded = false;
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
            packageName = modal.getPackageOrUrl();
        }
    }

    public void show() {
        mContext.startActivity(new Intent(mContext, InterstitialActivity.class));
        if (mContext instanceof AppCompatActivity) ((AppCompatActivity) mContext).overridePendingTransition(0, 0);
    }


    @SuppressWarnings("unused")
    public void show(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        mContext.startActivity(new Intent(mContext, InterstitialActivity.class));
        if (mContext instanceof AppCompatActivity) ((AppCompatActivity) mContext).overridePendingTransition(enterAnim, exitAnim);
    }

    public static class InterstitialActivity extends Activity {

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (mAdListener != null) mAdListener.onAdShown();

            setContentView(R.layout.house_ads_interstitial_layout);
            ImageView imageView = findViewById(R.id.image);
            ImageButton button = findViewById(R.id.button_close);

            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(view -> {
                isAdLoaded = false;
                if (packageName.startsWith("http")) {
                    Intent val = new Intent(Intent.ACTION_VIEW, Uri.parse(packageName));
                    val.setPackage("com.android.chrome");
                    if (val.resolveActivity(getPackageManager()) != null) startActivity(val);
                    else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(packageName)));

                    if (mAdListener != null) mAdListener.onApplicationLeft();
                    finish();
                }
                else {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        if (mAdListener != null) mAdListener.onApplicationLeft();
                        finish();
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                        if (mAdListener != null) mAdListener.onApplicationLeft();
                        finish();
                    }
                }
            });
            button.setOnClickListener(view -> {
                finish();
                isAdLoaded = false;
                if (mAdListener != null) mAdListener.onAdClosed();
            });
        }

        @Override
        public void onBackPressed() {
            isAdLoaded = false;
            if (mAdListener != null) mAdListener.onAdClosed();
            finish();
        }
    }
}
