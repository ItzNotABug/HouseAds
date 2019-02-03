package com.lazygeniouz.house.ads.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lazygeniouz.house.ads.HouseAdsNative;
import com.lazygeniouz.house.ads.listener.NativeAdListener;
import com.lazygeniouz.house.ads.sample.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NativeAd extends Fragment {

    public NativeAd() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.native_ad, container, false);

        rootView.findViewById(R.id.card_view).setVisibility(View.GONE);
        HouseAdsNative houseAdsNative = new HouseAdsNative(getContext(), "https://www.lazygeniouz.com/houseAds/ads.json");
        houseAdsNative.setNativeAdView(rootView.findViewById(R.id.card_view));
        houseAdsNative.usePalette(true);
        houseAdsNative.setNativeAdListener(new NativeAdListener() {
            @Override
            public void onAdLoaded() {
                rootView.findViewById(R.id.card_view).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdLoadFailed(Exception e) {

            }
        });

        Button load = rootView.findViewById(R.id.load);
        load.setOnClickListener(v -> {
            rootView.findViewById(R.id.card_view).setVisibility(View.GONE);
            houseAdsNative.loadAds();
        });

        return rootView;
    }
}
