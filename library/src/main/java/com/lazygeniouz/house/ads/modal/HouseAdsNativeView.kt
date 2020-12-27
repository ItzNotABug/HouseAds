/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.modal

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

// TODO: Move to a data class

/**
 * Much like the UnifiedNativeAdView,
 *
 * Create your custom ad-layout and
 * pass the relevant ui views / widget to this class
 */
class HouseAdsNativeView {
    var titleView: TextView? = null
    var descriptionView: TextView? = null
    var priceView: TextView? = null
    var iconView: ImageView? = null
    var headerImageView: ImageView? = null
    var callToActionView: View? = null
    var ratingsView: RatingBar? = null
}
