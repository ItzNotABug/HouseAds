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
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.lazygeniouz.house.ads.HouseAdsDialog;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.sample.R;

public class DialogAd extends Fragment implements AdListener {
    public DialogAd() {}

    private SwitchCompat hideIfAppInstalled;
    private SwitchCompat usePalette;
    private SwitchCompat hideHeader;
    private EditText cardCorner, ctaCorner;
    private HouseAdsDialog dialog;
    private TextView loading;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog, container, false);

        dialog = new HouseAdsDialog(getContext(), "https://www.lazygeniouz.com/houseAds/ads.json");
        dialog.setAdListener(this);
        //noinspection ConstantConditions
        dialog.setForceLoadFresh(getContext().getSharedPreferences("forceRefresh", Context.MODE_PRIVATE).getBoolean("val", false));

        SwitchCompat forceRefresh = rootView.findViewById(R.id.forceRefresh);
        hideIfAppInstalled = rootView.findViewById(R.id.hideIfInstalled);
        usePalette = rootView.findViewById(R.id.usePalette);
        hideHeader = rootView.findViewById(R.id.showHeader);
        cardCorner = rootView.findViewById(R.id.cardCorner);
        ctaCorner = rootView.findViewById(R.id.ctaCorner);
        loading = rootView.findViewById(R.id.loading);

        forceRefresh.setChecked(getContext().getSharedPreferences("forceRefresh", Context.MODE_PRIVATE).getBoolean("val", false));
        forceRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getContext().getSharedPreferences("forceRefresh", Context.MODE_PRIVATE).edit().putBoolean("val", isChecked).apply();
            //noinspection ConstantConditions
            getActivity().recreate();
        });

        MaterialButton loadAds = rootView.findViewById(R.id.load);
        loadAds.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            dialog.hideIfAppInstalled(hideIfAppInstalled.isChecked());
            dialog.usePalette(usePalette.isChecked());
            dialog.showHeaderIfAvailable(hideHeader.isChecked());
            dialog.setCardCorners(Integer.valueOf(cardCorner.getText().toString()));
            dialog.setCtaCorner(Integer.valueOf(ctaCorner.getText().toString()));
            dialog.loadAds();
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
