package com.lazygeniouz.house.ads.sample;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.lazygeniouz.house.ads.sample.fragments.DialogAd;
import com.lazygeniouz.house.ads.sample.fragments.InterstitialAd;
import com.lazygeniouz.house.ads.sample.fragments.NativeAd;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DialogAd(), "Dialog");
        adapter.addFragment(new InterstitialAd(), "Interstitial");
        adapter.addFragment(new NativeAd(), "Native");
        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentName = new ArrayList<>();
        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String name) {
            mFragmentList.add(fragment);
            mFragmentName.add(name);
        }

        @Override public CharSequence getPageTitle(int position) {
            return mFragmentName.get(position);
        }
    }
}
