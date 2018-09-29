package com.lazygeniouz.houseAds.sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lazygeniouz.house.ads.HouseAdsDialog;
import com.lazygeniouz.house.ads.HouseAdsInterstitial;
import com.lazygeniouz.house.ads.helper.HouseAdsHelper;
import com.lazygeniouz.house.ads.listener.AdListener;

public class MainActivity extends AppCompatActivity {

    private HouseAdsInterstitial interstitial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String adURL = "https://www.lazygeniouz.com/houseAds/ads.json";
        final TextView txt = findViewById(R.id.txt);

        final HouseAdsDialog houseAds = new HouseAdsDialog(MainActivity.this);
        houseAds.setUrl(adURL);
        houseAds.hideIfAppInstalled(true);
        houseAds.setCardCorners(100);
        houseAds.setCtaCorner(100);
        houseAds.setForceLoadFresh(false);
        houseAds.showHeaderIfAvailable(false);
        houseAds.setAdListener(new AdListener() {
            @Override
            public void onAdLoadFailed() {
                houseAds.loadAds();
            }

            @Override
            public void onAdLoaded() {
                houseAds.showAd();
            }

            @Override
            public void onAdClosed() {}

            @Override
            public void onAdShown() {
                Toast.makeText(MainActivity.this, "AdShown", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApplicationLeft() {}
        });




        final HouseAdsDialog houseAdsDialog = new HouseAdsDialog(this);
        houseAdsDialog.setUrl(adURL);
        houseAdsDialog.setCardCorners(50);
        houseAdsDialog.setCtaCorner(50);
        houseAdsDialog.setForceLoadFresh(true);
        houseAdsDialog.showHeaderIfAvailable(true);
        houseAdsDialog.setAdListener(new AdListener() {
            @Override
            public void onAdLoadFailed() {
                houseAdsDialog.loadAds();
            }

            @Override
            public void onAdLoaded() {
                houseAdsDialog.showAd();
            }

            @Override
            public void onAdClosed() {}

            @Override
            public void onAdShown() {
                Toast.makeText(MainActivity.this, "AdShown", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onApplicationLeft() {}
        });

        interstitial = new HouseAdsInterstitial(MainActivity.this);
        interstitial.setUrl(adURL);
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoadFailed() {
                interstitial.loadAd();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onAdLoaded() {
                txt.setText("Interstitial Ad Loaded");
                findViewById(R.id.button3).setEnabled(true);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onAdClosed() {
                txt.setText("Interstitial Ad Closed");
                findViewById(R.id.button3).setEnabled(false);
                interstitial.loadAd();
            }

            @Override
            public void onAdShown() {
                Toast.makeText(MainActivity.this, "AdShown", Toast.LENGTH_SHORT).show();

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onApplicationLeft() {
                txt.setText("Interstitial Ad Closed");
                findViewById(R.id.button3).setEnabled(false);
                interstitial.loadAd();
                Toast.makeText(MainActivity.this, "Application Left", Toast.LENGTH_SHORT).show();
            }
        });
        interstitial.loadAd();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                houseAds.loadAds();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                houseAdsDialog.loadAds();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitial.isAdLoaded()) interstitial.show();
            }
        });

        findViewById(R.id.clearCache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HouseAdsHelper.clearGlideCache(MainActivity.this);
            }
        });

        findViewById(R.id.nativeActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NativeAdActivity.class));
            }
        });
    }

}

