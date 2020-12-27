package com.lazygeniouz.house.ads.sample.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.lazygeniouz.house.ads.HouseAdsDialog
import com.lazygeniouz.house.ads.listener.AdListener
import com.lazygeniouz.house.ads.sample.R
import com.lazygeniouz.house.ads.sample.fragments.base.BaseFragment

class DialogAd : BaseFragment(), AdListener {
    private lateinit var hideIfAppInstalled: SwitchCompat
    private lateinit var usePalette: SwitchCompat
    private lateinit var hideHeader: SwitchCompat
    private lateinit var allCaps: SwitchCompat
    private lateinit var cardCorner: EditText
    private lateinit var ctaCorner: EditText
    private lateinit var dialog: HouseAdsDialog
    private lateinit var loading: TextView

    override fun getLayoutId(): Int = R.layout.dialog

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        val isShowLocalAssets = requireContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).getBoolean("value", false)
        dialog = if (isShowLocalAssets) HouseAdsDialog(requireActivity(), R.raw.ad_assets) else HouseAdsDialog(requireActivity(), "https://lz-houseads.firebaseapp.com/houseAds/ads.json")
        dialog.setAdListener(this@DialogAd)

        val localAssets: SwitchCompat = rootView.findViewById(R.id.useLocalResources)
        hideIfAppInstalled = rootView.findViewById(R.id.hideIfInstalled)
        usePalette = rootView.findViewById(R.id.usePalette)
        hideHeader = rootView.findViewById(R.id.showHeader)
        allCaps = rootView.findViewById(R.id.isAllCaps)
        cardCorner = rootView.findViewById(R.id.cardCorner)
        ctaCorner = rootView.findViewById(R.id.ctaCorner)
        loading = rootView.findViewById(R.id.loading)
        localAssets.isChecked = requireContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).getBoolean("value", false)
        localAssets.setOnCheckedChangeListener { _, isChecked ->
            requireContext().getSharedPreferences("localAssets", Context.MODE_PRIVATE).edit().putBoolean("value", isChecked).apply()
            requireActivity().recreate()
        }
        val loadAds: MaterialButton = rootView.findViewById(R.id.load)
        loadAds.setOnClickListener {
            loading.visibility = View.VISIBLE
            dialog.apply {
                hideIfAppInstalled(hideIfAppInstalled.isChecked)
                usePalette(usePalette.isChecked)
                showHeaderIfAvailable(hideHeader.isChecked)
                setCardCorners(cardCorner.text.toString().toInt())
                setCtaCorner(ctaCorner.text.toString().toInt())
                ctaAllCaps(allCaps.isChecked)
                loadAds()
            }
        }
    }

    override fun onAdFailedToLoad(exception: Exception) {
        loading.visibility = View.GONE
        Log.d("HouseAdsExample", "Error: ${exception.message}")
    }

    override fun onAdLoaded() {
        dialog.showAd()
    }

    override fun onAdClosed() {
        Log.d("HouseAdsExample", "Ad Closed")
    }

    override fun onAdShown() {
        loading.visibility = View.GONE
        Log.d("HouseAdsExample", "Ad Shown")
    }

    override fun onApplicationLeft() {
        Log.d("HouseAdsExample", "Application Left")
    }
}