/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads.modal

import java.lang.Float.parseFloat

class DialogModal {
    var iconUrl: String? = null
    var appTitle: String? = null
    var appDesc: String? = null
    var largeImageUrl: String? = null
    var packageOrUrl: String? = null
    var callToActionButtonText: String? = null
    var price: String? = null
    private var rating: String? = null

    fun setRating(ratings: String) {
        this.rating = ratings
    }

    fun getRating(): Float {
        var ratings = 0f
        if (rating != null && rating!!.isNotEmpty()) ratings = parseFloat(rating!!)
        return ratings
    }
}
