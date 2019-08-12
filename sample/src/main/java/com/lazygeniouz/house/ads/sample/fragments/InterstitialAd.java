package com.lazygeniouz.house.ads.sample.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.lazygeniouz.house.ads.HouseAdsInterstitial;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.sample.R;
import com.lazygeniouz.house.ads.sample.listener.BackPressListener;


public class InterstitialAd extends Fragment implements AdListener, BackPressListener {

    public InterstitialAd() {}

    private HouseAdsInterstitial interstitial;
    private TextView interstitialStatus;
    private MaterialButton load, show;
    private SwitchCompat isFromBackPress;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mContext = getContext();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.interstitial, container, false);
        interstitial = new HouseAdsInterstitial(mContext, "https://lz-houseads.firebaseapp.com/houseAds/ads.json");
        interstitial.setAdListener(this);

        isFromBackPress = rootView.findViewById(R.id.showOnBackPress);
        load = rootView.findViewById(R.id.load);
        show = rootView.findViewById(R.id.show);
        interstitialStatus = rootView.findViewById(R.id.interstitial_status);
        interstitialStatus.setVisibility(View.GONE);
        show.setEnabled(false);
        show.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e5e5e5")));

        show.setOnClickListener(v -> interstitial.show());
        load.setOnClickListener(v -> {
            interstitialStatus.setVisibility(View.VISIBLE);
            interstitialStatus.setText(getString(R.string.ad_loading));
            interstitial.loadAd();
            load.setEnabled(false);
            load.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e5e5e5")));
        });
        return rootView;
    }

    @Override
    public void onAdLoadFailed(Exception e) {
        interstitialStatus.setVisibility(View.VISIBLE);
        interstitialStatus.setText(String.format("%s%s", getString(R.string.ad_failed), e.getMessage()));
        load.setEnabled(true);
        load.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
    }

    @Override
    public void onAdLoaded() {
        if (!isFromBackPress.isChecked()) {
            show.setEnabled(true);
            show.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
            interstitialStatus.setVisibility(View.GONE);
        }
        else {
            interstitialStatus.setText(getString(R.string.press_back));
            interstitialStatus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAdClosed() {
        interstitialStatus.setVisibility(View.GONE);
        load.setEnabled(true);
        load.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
        show.setEnabled(false);
        show.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e5e5e5")));
        Toast.makeText(mContext, "Ad Closed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdShown() {
        Toast.makeText(mContext, "Ad Shown", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApplicationLeft() {
        interstitialStatus.setVisibility(View.GONE);
        load.setEnabled(true);
        load.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
        show.setEnabled(false);
        show.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e5e5e5")));
        Toast.makeText(mContext, "Application Left", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(AppCompatActivity activity) {
        if (isFromBackPress.isChecked() && interstitial.isAdLoaded()) interstitial.show();
        else activity.finish();
    }
}
