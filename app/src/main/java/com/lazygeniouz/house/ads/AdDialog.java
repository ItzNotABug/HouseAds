package com.lazygeniouz.house.ads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lazygeniouz.house.ads.listener.AdListener;

public class AdDialog extends AppCompatActivity {

    private HouseAdsInterstitial interstitial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView txt = findViewById(R.id.txt);

        final HouseAdsDialog houseAds = new HouseAdsDialog(AdDialog.this);
        final HouseAdsDialog houseAdsDialog = new HouseAdsDialog(this);

        interstitial = new HouseAdsInterstitial(AdDialog.this);
        interstitial.addListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                txt.setText("Interstitial Ad Loaded");
                findViewById(R.id.button3).setEnabled(true);
            }

            @Override
            public void onAdClosed() {
                txt.setText("Interstitial Ad Closed");
                findViewById(R.id.button3).setEnabled(false);
                interstitial.loadAd();
            }

            @Override
            public void onAdShown() {
                Toast.makeText(AdDialog.this, "AdShown", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onApplicationLeft() {
                Toast.makeText(AdDialog.this, "Application Left", Toast.LENGTH_SHORT).show();
            }
        });
        interstitial.loadAd();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                houseAds.setForceLoadFresh(false);
                houseAds.loadAds();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                houseAdsDialog.setForceLoadFresh(true);
                houseAdsDialog.loadAds();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitial.isAdLoaded()) interstitial.show();
            }
        });
    }

}
