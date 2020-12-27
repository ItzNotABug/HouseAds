/*
 * Created by Darshan Pandya. (@itznotabug)
 * Copyright (c) 2018-2020.
 */

package com.lazygeniouz.house.ads.modal

// TODO: Move to a data class

internal class DialogModal {
    var iconUrl: String? = null
    var appTitle: String? = null
    var appDesc: String? = null
    var largeImageUrl: String? = null
    var packageOrUrl: String? = null
    var callToActionButtonText: String? = null
    var price: String? = null
    var rating: String? = null

    internal fun getRating(): Float {
        var ratings = 0f
        if (rating != null && rating!!.isNotEmpty()) ratings = rating!!.toFloat()
        return ratings
    }
}
