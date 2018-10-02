/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.houseAds.sample;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lazygeniouz.house.ads.HouseAdsNative;
import com.lazygeniouz.house.ads.listener.NativeAdListener;
import com.lazygeniouz.house.ads.modal.HouseAdsNativeView;

public class NativeAdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.native_activity);
        final CardView card = findViewById(R.id.card_view);
        final Button load = findViewById(R.id.load);
        GradientDrawable drawable = (GradientDrawable) findViewById(R.id.call_to_action).getBackground();
        drawable.setCornerRadius(100);

        HouseAdsNativeView nativeView = new HouseAdsNativeView();
        nativeView.setTitleView((TextView) findViewById(R.id.appinstall_headline));
        nativeView.setDescriptionView((TextView) findViewById(R.id.appinstall_body));
        nativeView.setIconView((ImageView) findViewById(R.id.appinstall_app_icon));
        nativeView.setHeaderImageView((ImageView) findViewById(R.id.large));
        nativeView.setCallToActionView(findViewById(R.id.call_to_action));
        nativeView.setPriceView((TextView) findViewById(R.id.price));
        nativeView.setRatingsView((RatingBar) findViewById(R.id.ratings));

        final HouseAdsNative houseAdsNative = new HouseAdsNative(NativeAdActivity.this);
        houseAdsNative.setNativeAdView(nativeView);
        houseAdsNative.setUrl("https://www.lazygeniouz.com/houseAds/ads.json");
        houseAdsNative.setNativeAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded() {
                card.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoadFailed() {
                Toast.makeText(NativeAdActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (card.getVisibility() == View.VISIBLE) card.setVisibility(View.GONE);
                houseAdsNative.loadAds();
            }
        });
    }
}
