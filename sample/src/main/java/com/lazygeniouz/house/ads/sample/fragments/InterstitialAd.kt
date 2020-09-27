package com.lazygeniouz.house.ads.sample.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.lazygeniouz.house.ads.HouseAdsInterstitial
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.sample.R

class InterstitialAd : BaseFragment(), AdListener {
    private lateinit var interstitial: HouseAdsInterstitial
    private lateinit var interstitialStatus: TextView
    private lateinit var load: MaterialButton
    private lateinit var show: MaterialButton
    private lateinit var isFromBackPress: SwitchCompat
    private lateinit var closeOnBackPress: SwitchCompat
    private lateinit var usePalette: SwitchCompat
    private lateinit var hideNavigation: SwitchCompat


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.interstitial, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        val isShowLocalAssets = requireContext().getSharedPreferences("localAssetsInterstitial", Context.MODE_PRIVATE).getBoolean("value", false)
        interstitial = if (!isShowLocalAssets) HouseAdsInterstitial(requireContext(), "https://lz-houseads.firebaseapp.com/houseAds/ads.json") else HouseAdsInterstitial(requireContext(), R.raw.ad_assets)
        interstitial.setAdListener(this)
        val localAssets: SwitchCompat = rootView.findViewById(R.id.useLocalResources)
        isFromBackPress = rootView.findViewById(R.id.showOnBackPress)
        closeOnBackPress = rootView.findViewById(R.id.closeOnBackPress)
        usePalette = rootView.findViewById(R.id.usePalette)
        hideNavigation = rootView.findViewById(R.id.hideNavigation)
        load = rootView.findViewById(R.id.load)
        show = rootView.findViewById(R.id.show)
        interstitialStatus = rootView.findViewById(R.id.interstitial_status)
        interstitialStatus.visibility = View.GONE
        show.isEnabled = false
        show.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e5e5e5"))
        show.setOnClickListener { interstitial.show() }
        load.setOnClickListener {
            interstitialStatus.visibility = View.VISIBLE
            interstitialStatus.text = getString(R.string.ad_loading)
            interstitial.apply {
                usePalette(usePalette.isChecked)
                hideNavigationBar(hideNavigation.isChecked)
                exitOnBackPress(closeOnBackPress.isChecked)
                setCloseButtonColor(Color.BLACK, Color.WHITE)
                loadAds()
            }
            load.isEnabled = false
            load.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e5e5e5"))
        }
        localAssets.isChecked = requireContext().getSharedPreferences("localAssetsInterstitial", Context.MODE_PRIVATE).getBoolean("value", false)
        localAssets.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences("localAssetsInterstitial", Context.MODE_PRIVATE).edit().putBoolean("value", isChecked).apply()
            requireActivity().recreate()
        }
    }

    override fun onAdLoadFailed(exception: Exception) {
        interstitialStatus.visibility = View.VISIBLE
        interstitialStatus.text = String.format("%s%s", getString(R.string.ad_failed), exception.message)
        load.isEnabled = true
        load.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
    }

    override fun onAdLoaded() {
        if (!isFromBackPress.isChecked) {
            show.isEnabled = true
            show.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            interstitialStatus.visibility = View.GONE
        } else {
            interstitialStatus.text = getString(R.string.press_back)
            interstitialStatus.visibility = View.VISIBLE
        }
    }

    override fun onAdClosed() {
        interstitialStatus.visibility = View.GONE
        load.isEnabled = true
        load.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        show.isEnabled = false
        show.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e5e5e5"))
        Toast.makeText(requireContext(), "Ad Closed", Toast.LENGTH_SHORT).show()
    }

    override fun onAdShown() {
        Toast.makeText(requireContext(), "Ad Shown", Toast.LENGTH_SHORT).show()
    }

    override fun onApplicationLeft() {
        interstitialStatus.visibility = View.GONE
        load.isEnabled = true
        load.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        show.isEnabled = false
        show.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#e5e5e5"))
        Toast.makeText(requireContext(), "Application Left", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed(activity: AppCompatActivity) {
        if (isFromBackPress.isChecked && interstitial.isAdLoaded()) interstitial.show() else activity.finish()
    }
}