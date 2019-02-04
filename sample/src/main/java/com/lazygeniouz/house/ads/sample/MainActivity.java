package com.lazygeniouz.house.ads.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.lazygeniouz.checkoutverifier.CheckoutVerifier;
import com.lazygeniouz.checkoutverifier.VerifyingListener;
import com.lazygeniouz.house.ads.sample.fragments.DialogAd;
import com.lazygeniouz.house.ads.sample.fragments.InterstitialAd;
import com.lazygeniouz.house.ads.sample.fragments.NativeAd;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter adapter;
    private ViewPager viewPager;
    private BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        billingProcessor = new BillingProcessor(MainActivity.this, null, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(View.inflate(MainActivity.this, R.layout.verify_purchase, null))
                        .create();

                new CheckoutVerifier("https://www.lazygeniouz.com/iab-verify/houseAds/verify.php",
                        details.purchaseInfo.responseData,
                        details.purchaseInfo.signature,
                        new VerifyingListener() {
                            @Override
                            public void onVerificationStarted() {
                                dialog.show();
                            }

                            @Override
                            public void onVerificationCompleted(boolean isVerified) {
                                dialog.dismiss();
                                if (isVerified) showSnackbar("Thank You! :)");
                                else showSnackbar("Error! :(");
                            }

                            @Override
                            public void onExceptionCaught(@NotNull Exception e) {
                                showSnackbar("Error1 :(");
                            }
                        }).start();
            }

            @Override
            public void onPurchaseHistoryRestored() {

            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {

            }

            @Override
            public void onBillingInitialized() {

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        FloatingActionButton donate = findViewById(R.id.donate);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        donate.setOnClickListener(v -> {
            //Show a Dialog with 3 Donations..
            //Small = 150, Large = 500
            View donation = View.inflate(MainActivity.this, R.layout.donation, null);
            AlertDialog donateDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Donate")
                    .setView(donation)
                    .create();

            donation.findViewById(R.id.small).setOnClickListener(v13 -> {
                donateDialog.dismiss();
                billingProcessor.purchase(MainActivity.this, "small");
            });
            donation.findViewById(R.id.medium).setOnClickListener(v12 -> {
                donateDialog.dismiss();
                billingProcessor.purchase(MainActivity.this, "medium");
            });
            donation.findViewById(R.id.large).setOnClickListener(v1 -> {
                donateDialog.dismiss();
                billingProcessor.purchase(MainActivity.this, "large");
            });

            donateDialog.show();
            donateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
            donateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
            donateDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setAllCaps(false);
        });
    }

    void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator), msg, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) snackbarView.setBackground(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorAccent)));
        else snackbarView.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this, R.color.colorAccent)));
        ((TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)).setGravity(Gravity.CENTER);
        ((TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)).setTypeface(Typeface.SERIF, Typeface.BOLD);
        snackbar.show();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DialogAd(), "Dialog");
        adapter.addFragment(new InterstitialAd(), "Interstitial");
        adapter.addFragment(new NativeAd(), "Native");
        viewPager.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            ((InterstitialAd) adapter.getItem(1)).onBackPressed(MainActivity.this);
        } else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (billingProcessor != null) billingProcessor.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.git:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/itznotabug/houseAds")));
                return true;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentName = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String name) {
            mFragmentList.add(fragment);
            mFragmentName.add(name);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentName.get(position);
        }
    }
}
