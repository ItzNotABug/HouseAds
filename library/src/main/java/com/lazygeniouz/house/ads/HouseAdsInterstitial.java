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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.modal.InterstitialModal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HouseAdsInterstitial {
    private Context mContext;
    private static AdListener mAdListener;
    private String url;
    private int lastLoaded = 0;

    private static boolean isAdLoaded = false;

    private static Bitmap bitmap;
    private static String packageName;

    public HouseAdsInterstitial(Context context) {
        this.mContext = context;
    }

    public void addListener(AdListener adListener) {
        mAdListener = adListener;
    }

    public void setUrl(String val) {
        this.url = val;
    }

    public void loadAd() {
        if (url.trim().equals("")) throw new IllegalArgumentException("Url is Blank!");
        else new ScanUrlTask(url).execute();
    }

    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    @SuppressLint("StaticFieldLeak")
    private class ScanUrlTask extends AsyncTask<String, String, String> {
        String url;

        ScanUrlTask(String url) {
            isAdLoaded = false;
            this.url = url;
        }

        @Override
        protected String doInBackground(String... p1) {
            return Helper.parseJsonObject(url);
        }

        @Override
        protected void onPostExecute(String result) {
            setUp(result);
        }
    }

    private void setUp(String val) {
        ArrayList<InterstitialModal> modalArrayList = new ArrayList<>();

        String x = new String(new StringBuilder().append(val));

        try {
            JSONObject rootObject = new JSONObject(x);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                JSONObject jsonObject = array.getJSONObject(object);
                InterstitialModal interstitialModal = new InterstitialModal();

                interstitialModal.setInterstitialImageUrl(jsonObject.optString("app_interstitial_url"));
                interstitialModal.setPackageName(jsonObject.optString("app_package"));
                modalArrayList.add(interstitialModal);
            }

        } catch (JSONException e) { e.printStackTrace(); }

        if (modalArrayList.size() > 0) {
            final InterstitialModal modal = modalArrayList.get(lastLoaded);
            if (lastLoaded == modalArrayList.size() - 1) lastLoaded = 0;
            else lastLoaded++;

            Glide.with(mContext).asBitmap().load(modal.getInterstitialImageUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap GBitmap, @Nullable Transition<? super Bitmap> transition) {
                    bitmap = GBitmap;
                    if (mAdListener != null) mAdListener.onAdLoaded();
                    isAdLoaded = true;
                }
            });
            packageName = modal.getPackageName();
        }
    }

    public void show() {
        //InterstitialActivity activity = new InterstitialActivity();
        mContext.startActivity(new Intent(mContext, InterstitialActivity.class));
        if (mContext instanceof AppCompatActivity) ((AppCompatActivity) mContext).overridePendingTransition(0, 0);
    }

    public static class InterstitialActivity extends Activity {

        //InterstitialActivity() {}

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (mAdListener != null) mAdListener.onAdShown();

            setContentView(R.layout.interstitial);
            ImageView imageView = findViewById(R.id.image);
            ImageButton button = findViewById(R.id.button_close);

            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAdListener != null) mAdListener.onApplicationLeft();
                    isAdLoaded = false;
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                    try {
                        startActivity(goToMarket);
                        finish();
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                        finish();
                    }
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                    isAdLoaded = false;
                    if (mAdListener != null) mAdListener.onAdClosed();
                }
            });
        }

        @Override
        public void onBackPressed() {
            isAdLoaded = false;
            if (mAdListener != null) mAdListener.onAdClosed();
            super.onBackPressed();
        }
    }
}
