package com.lazygeniouz.house.ads.sample.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.lazygeniouz.house.ads.HouseAdsNative
import com.lazygeniouz.house.ads.listener.NativeAdListener
import com.lazygeniouz.house.ads.sample.R

class NativeAd : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.native_ad, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        val isShowLocalAssets = requireContext().getSharedPreferences("localAssetsNative", Context.MODE_PRIVATE).getBoolean("value", false)
        rootView.findViewById<View>(R.id.card_view).visibility = View.GONE
        val loading = rootView.findViewById<TextView>(R.id.loading)
        val localAssets: SwitchCompat = rootView.findViewById(R.id.useLocalResources)
        val houseAdsNative: HouseAdsNative
        if (!isShowLocalAssets) {
            rootView.findViewById<View>(R.id.houseAds_header_image).visibility = View.GONE
            houseAdsNative = HouseAdsNative(requireContext(), "https://lz-houseads.firebaseapp.com/houseAds/ads.json")
        } else houseAdsNative = HouseAdsNative(requireContext(), R.raw.ad_assets)
        houseAdsNative.setNativeAdView(rootView.findViewById(R.id.card_view))
                .usePalette(true)
                .setNativeAdListener(object : NativeAdListener {
                    override fun onAdLoaded() {
                        loading.visibility = View.GONE
                        rootView.findViewById<View>(R.id.card_view).visibility = View.VISIBLE
                    }

                    override fun onAdLoadFailed(exception: Exception) {
                        loading.text = String.format("%s%s", getString(R.string.ad_failed), exception.message)
                        loading.visibility = View.VISIBLE
                    }
                })
        val load: MaterialButton = rootView.findViewById(R.id.load)
        load.setOnClickListener {
            rootView.findViewById<View>(R.id.card_view).visibility = View.GONE
            loading.visibility = View.VISIBLE
            houseAdsNative.loadAds()
        }
        localAssets.isChecked = requireContext().getSharedPreferences("localAssetsNative", Context.MODE_PRIVATE).getBoolean("value", false)
        localAssets.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences("localAssetsNative", Context.MODE_PRIVATE).edit().putBoolean("value", isChecked).apply()
            requireActivity().recreate()
        }
    }
}