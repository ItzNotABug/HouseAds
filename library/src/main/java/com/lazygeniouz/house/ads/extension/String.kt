package com.lazygeniouz.house.ads.extension

val String.hasHttpSign: Boolean
    get() {
        return this.startsWith("http")
    }

val String.hasDrawableSign: Boolean
    get() {
        return this.startsWith("@drawable/")
    }