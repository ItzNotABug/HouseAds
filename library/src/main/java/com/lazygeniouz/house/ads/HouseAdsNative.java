/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.lazygeniouz.house.ads.helper.HouseAdsHelper;
import com.lazygeniouz.house.ads.listener.NativeAdListener;
import com.lazygeniouz.house.ads.modal.DialogModal;
import com.lazygeniouz.house.ads.modal.HouseAdsNativeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class HouseAdsNative {
    private final Context mContext;
    private String jsonUrl;
    //private String jsonRawResponse = "";

    private boolean isAdLoaded = false;
    private boolean hideIfAppInstalled  = false;
    private static int lastLoaded = 0;

    private HouseAdsNativeView nativeAdView;
    private NativeAdListener mNativeAdListener;
    private NativeAdListener.CallToActionListener ctaListener;

    public HouseAdsNative(Context context) {
        this.mContext = context;
    }

    public void setUrl(String url) {
        this.jsonUrl = url;
    }
    
    public void setNativeAdView(HouseAdsNativeView nativeAdView) {
        this.nativeAdView = nativeAdView;
    }
    
    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    public void hideIfAppInstalled(boolean val) {
        this.hideIfAppInstalled = val;
    }
    
    public void setNativeAdListener(NativeAdListener listener) {
        this.mNativeAdListener = listener;
    }
    
    public void setCallToActionListener(NativeAdListener.CallToActionListener listener) {
        this.ctaListener = listener;
    }
    
    public void loadAds() {
        isAdLoaded = false;
        new ScanUrl(jsonUrl).execute();
        /*if (jsonUrl.trim().equals("")) throw new IllegalArgumentException("Url is Blank!");
        else {
            if (jsonRawResponse.equals(""))
        }*/
    }



    @SuppressLint("NewApi")
    private void setUp(String response) {
        ArrayList<DialogModal> val = new ArrayList<>();

        try {
            JSONObject rootObject = new JSONObject(response);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                final JSONObject jsonObject = array.getJSONObject(object);


                if (hideIfAppInstalled && !jsonObject.optString("app_uri").startsWith("http") &&  HouseAdsHelper.isAppInstalled(mContext, jsonObject.optString("app_uri"))) array.remove(object);
                    //ToDo: Handle remove() on pre 19!
                else {
                    //We Only Add Native Ones!
                    if (jsonObject.optString("app_adType").equals("native")) {
                        final DialogModal dialogModal = new DialogModal();
                        dialogModal.setAppTitle(jsonObject.optString("app_title"));
                        dialogModal.setAppDesc(jsonObject.optString("app_desc"));
                        dialogModal.setIconUrl(jsonObject.optString("app_icon"));
                        dialogModal.setLargeImageUrl(jsonObject.optString("app_header_image"));
                        dialogModal.setCtaText(jsonObject.optString("app_cta_text"));
                        dialogModal.setPackageOrUrl(jsonObject.optString("app_uri"));
                        dialogModal.setRating(jsonObject.optInt("app_rating"));
                        dialogModal.setPrice(jsonObject.optString("app_price"));

                        val.add(dialogModal);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (val.size() > 0) {
            final DialogModal dialogModal = val.get(lastLoaded);
            if (lastLoaded == val.size() - 1) lastLoaded = 0;
            else lastLoaded++;

            final HouseAdsNativeView view = nativeAdView;

            if (dialogModal.getIconUrl().trim().equals("") || !dialogModal.getIconUrl().trim().contains("http")) throw new IllegalArgumentException("Icon URL should not be Null or Blank & should start with \"http\"");
            if (!dialogModal.getLargeImageUrl().trim().equals("") && !dialogModal.getIconUrl().trim().contains("http")) throw new IllegalArgumentException("Header Image URL should start with \"http\"");
            if (dialogModal.getAppTitle().trim().equals("") || dialogModal.getAppDesc().trim().equals("")) throw new IllegalArgumentException("Title & description should not be Null or Blank.");

            final View cta = view.getCallToActionView();

            final ImageView icon = view.getIconView();
            final ImageView headerImage = view.getHeaderImageView();
            TextView title = view.getTitleView();
            TextView description = view.getDescriptionView();
            final RatingBar ratings = view.getRatingsView();
            TextView price = view.getPriceView();

            Glide.with(mContext).load(dialogModal.getIconUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable GlideAnimation<? super Bitmap> transition) {
                    icon.setImageBitmap(resource);
                    if (dialogModal.getLargeImageUrl().trim().equals("")) {
                        isAdLoaded = true;
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoaded();
                    }
                }
            });

            if (dialogModal.getLargeImageUrl().trim().equals("")) headerImage.setVisibility(View.GONE);
            else headerImage.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(dialogModal.getLargeImageUrl()).asBitmap()/*.override(headerImage.getWidth(), headerImage.getHeight())*/.into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    headerImage.setImageBitmap(resource);
                    if (!dialogModal.getLargeImageUrl().trim().equals("")) {
                        isAdLoaded = true;
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoaded();
                    }
                }});

            title.setText(dialogModal.getAppTitle());
            description.setText(dialogModal.getAppDesc());
            if (price != null && !dialogModal.getPrice().trim().equals("")) price.setText(String.format("Price: %s", dialogModal.getPrice()));
            if (ratings != null && dialogModal.getRating() != 0) ratings.setRating(dialogModal.getRating());

            if (cta != null) {
                if (cta instanceof TextView) ((TextView) cta).setText(dialogModal.getCtaText());
                if (cta instanceof Button) ((Button) cta).setText(dialogModal.getCtaText());

                cta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ctaListener != null) ctaListener.onCallToActionClicked(view);
                        else {
                            String packageOrUrl = dialogModal.getPackageOrUrl();
                            if (packageOrUrl.trim().startsWith("http")) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)));
                            } else {
                                try {
                                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageOrUrl)));
                                } catch (ActivityNotFoundException e) {
                                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageOrUrl)));
                                }
                            }
                        }
                    }
                });
            }
            if (!(cta instanceof TextView)) throw new IllegalArgumentException("Call to Action View must be either a Button or a TextView");
        }

    }


    @SuppressLint("StaticFieldLeak")
    private class ScanUrl extends AsyncTask<String, String, String> {
        final String url;

        ScanUrl(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... p1) {
            return HouseAdsHelper.parseJsonObject(url);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.trim().equals("")) {
                //jsonRawResponse = result;
                setUp(result);
            }
            else {
                if (mNativeAdListener != null) mNativeAdListener.onAdLoadFailed();
            }
        }
    }
}
