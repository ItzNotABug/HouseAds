package com.lazygeniouz.house.ads.sample.fragments

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    open fun onBackPressed(activity: AppCompatActivity) {}
}