package com.lazygeniouz.house.ads.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.lazygeniouz.house.ads.HouseAdsNative;
import com.lazygeniouz.house.ads.listener.NativeAdListener;
import com.lazygeniouz.house.ads.sample.R;

public class NativeAd extends Fragment {

    public NativeAd() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.native_ad, container, false);

        rootView.findViewById(R.id.card_view).setVisibility(View.GONE);
        TextView loading = rootView.findViewById(R.id.loading);
        HouseAdsNative houseAdsNative = new HouseAdsNative(getContext(), "https://lz-houseads.firebaseapp.com/houseAds/ads.json");
        houseAdsNative.setNativeAdView(rootView.findViewById(R.id.card_view));
        houseAdsNative.usePalette(true);
        houseAdsNative.setNativeAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded() {
                loading.setVisibility(View.GONE);
                rootView.findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoadFailed(Exception e) {
                loading.setText(String.format("%s%s", getString(R.string.ad_failed), e.getMessage()));
                loading.setVisibility(View.VISIBLE);
            }
        });

        MaterialButton load = rootView.findViewById(R.id.load);
        load.setOnClickListener(v -> {
            rootView.findViewById(R.id.card_view).setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            houseAdsNative.loadAds();
        });

        return rootView;
    }
}
