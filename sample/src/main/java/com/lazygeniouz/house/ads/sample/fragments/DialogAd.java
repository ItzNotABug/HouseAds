package com.lazygeniouz.house.ads.sample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.lazygeniouz.house.ads.HouseAdsDialog;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.sample.R;

public class DialogAd extends BaseFragment implements AdListener {
    public DialogAd() {
    }

    private SwitchCompat hideIfAppInstalled, usePalette, hideHeader, allCaps;
    private EditText cardCorner, ctaCorner;
    private HouseAdsDialog dialog;
    private TextView loading;

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog, container, false);
        boolean isShowLocalAssets = getContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).getBoolean("value", false);

        if (isShowLocalAssets) dialog = new HouseAdsDialog(getContext(), R.raw.ad_assets);
        else dialog = new HouseAdsDialog(requireContext(), "https://lz-houseads.firebaseapp.com/houseAds/ads.json");
        dialog.setAdListener(DialogAd.this);

        SwitchCompat localAssets = rootView.findViewById(R.id.useLocalResources);

        hideIfAppInstalled = rootView.findViewById(R.id.hideIfInstalled);
        usePalette = rootView.findViewById(R.id.usePalette);
        hideHeader = rootView.findViewById(R.id.showHeader);
        allCaps = rootView.findViewById(R.id.isAllCaps);
        cardCorner = rootView.findViewById(R.id.cardCorner);
        ctaCorner = rootView.findViewById(R.id.ctaCorner);
        loading = rootView.findViewById(R.id.loading);


        localAssets.setChecked(getContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).getBoolean("value", false));
        localAssets.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).edit().putBoolean("value", isChecked).apply();
            getActivity().recreate();
        });

        MaterialButton loadAds = rootView.findViewById(R.id.load);
        loadAds.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            dialog.hideIfAppInstalled(hideIfAppInstalled.isChecked())
                    .usePalette(usePalette.isChecked())
                    .showHeaderIfAvailable(hideHeader.isChecked())
                    .setCardCorners(Integer.parseInt(cardCorner.getText().toString()))
                    .setCtaCorner(Integer.parseInt(ctaCorner.getText().toString()))
                    .ctaAllCaps(allCaps.isChecked())
                    .loadAds();
        });

        return rootView;
    }

    @Override
    public void onAdLoadFailed(Exception e) {
        loading.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdLoaded() {
        dialog.showAd();
    }

    @Override
    public void onAdClosed() {
        Toast.makeText(getContext(), "Ad Closed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdShown() {
        loading.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Ad Shown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApplicationLeft() {
        Toast.makeText(getContext(), "Application Left", Toast.LENGTH_SHORT).show();
    }
}
