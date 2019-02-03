package com.lazygeniouz.house.ads.sample.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lazygeniouz.house.ads.HouseAdsInterstitial;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


@SuppressLint("SetTextI18n")
public class InterstitialAd extends Fragment implements AdListener {

    public InterstitialAd() {
    }

    private TextView interstitialStatus;
    private Button load, show;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.interstitial, container, false);
        HouseAdsInterstitial interstitial = new HouseAdsInterstitial(getContext(), "https://www.lazygeniouz.com/houseAds/ads.json");
        interstitial.setAdListener(this);

        load = rootView.findViewById(R.id.load);
        show = rootView.findViewById(R.id.show);
        interstitialStatus = rootView.findViewById(R.id.interstitial_status);
        show.setEnabled(false);

        show.setOnClickListener(v -> interstitial.show());
        load.setOnClickListener(v -> {
            interstitialStatus.setText("Ad Loading...");
            interstitial.loadAd();
            load.setEnabled(false);
        });
        return rootView;
    }

    @Override
    public void onAdLoadFailed(Exception e) {
        interstitialStatus.setText("Ad Loading Failed.. \nReason:" + e.getMessage());
        load.setEnabled(true);
    }

    @Override
    public void onAdLoaded() {
        interstitialStatus.setText("Ad Loaded");
        show.setEnabled(true);
    }

    @Override
    public void onAdClosed() {
        interstitialStatus.setText("Ad Closed");
        load.setEnabled(true);
        show.setEnabled(false);
    }

    @Override
    public void onAdShown() {
        Toast.makeText(getContext(), "Ad Shown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApplicationLeft() {
        Toast.makeText(getContext(), "Application Left", Toast.LENGTH_SHORT).show();
    }
}
