/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;

import com.lazygeniouz.house.ads.helper.HouseAdsHelper;
import com.lazygeniouz.house.ads.helper.JsonPullerTask;
import com.lazygeniouz.house.ads.helper.RemoveJsonObjectCompat;
import com.lazygeniouz.house.ads.listener.NativeAdListener;
import com.lazygeniouz.house.ads.modal.DialogModal;
import com.lazygeniouz.house.ads.modal.HouseAdsNativeView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class HouseAdsNative {
    private final Context mContext;
    private final String jsonUrl;

    private boolean usePalette = true;
    private boolean isAdLoaded = false;
    private boolean hideIfAppInstalled = false;
    private static int lastLoaded = 0;

    private HouseAdsNativeView nativeAdView;
    private View customNativeView;
    private NativeAdListener mNativeAdListener;
    private NativeAdListener.CallToActionListener ctaListener;

    public HouseAdsNative(Context context, String url) {
        this.mContext = context;
        this.jsonUrl = url;
    }

    public void setNativeAdView(HouseAdsNativeView nativeAdView) {
        this.nativeAdView = nativeAdView;
    }

    public void setNativeAdView(View view) {
        this.customNativeView = view;
    }

    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    public void hideIfAppInstalled(boolean val) {
        this.hideIfAppInstalled = val;
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
    }

    public void setNativeAdListener(NativeAdListener listener) {
        this.mNativeAdListener = listener;
    }

    public void setCallToActionListener(NativeAdListener.CallToActionListener listener) {
        this.ctaListener = listener;
    }

    public void loadAds() {
        isAdLoaded = false;
        if (jsonUrl.trim().isEmpty()) throw new IllegalArgumentException("Url is Blank!");
        else new JsonPullerTask(jsonUrl, result -> {
            if (!result.trim().isEmpty()) setUp(result);
            else {
                if (mNativeAdListener != null) mNativeAdListener.onAdLoadFailed(new Exception("Null Response"));
            }
        }).execute();
    }

    private void setUp(String response) {
        ArrayList<DialogModal> val = new ArrayList<>();

        try {
            JSONObject rootObject = new JSONObject(response);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                final JSONObject jsonObject = array.getJSONObject(object);


                if (hideIfAppInstalled && !jsonObject.optString("app_uri").startsWith("http") && HouseAdsHelper.isAppInstalled(mContext, jsonObject.optString("app_uri")))
                    new RemoveJsonObjectCompat(object, array).execute();
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
                        dialogModal.setRating(jsonObject.optString("app_rating"));
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

            TextView title, description, price;
            final View cta;
            final ImageView icon, headerImage;
            final RatingBar ratings;


            if (nativeAdView != null) {
                final HouseAdsNativeView view = nativeAdView;
                title = view.getTitleView();
                description = view.getDescriptionView();
                price = view.getPriceView();
                cta = view.getCallToActionView();
                icon = view.getIconView();
                headerImage = view.getHeaderImageView();
                ratings = view.getRatingsView();
            } else {
                if (customNativeView != null) {
                    title = customNativeView.findViewById(R.id.houseAds_title);
                    description = customNativeView.findViewById(R.id.houseAds_description);
                    price = customNativeView.findViewById(R.id.houseAds_price);
                    cta = customNativeView.findViewById(R.id.houseAds_cta);
                    icon = customNativeView.findViewById(R.id.houseAds_app_icon);
                    headerImage = customNativeView.findViewById(R.id.houseAds_header_image);
                    ratings = customNativeView.findViewById(R.id.houseAds_rating);
                } else
                    throw new NullPointerException("NativeAdView is Null. Either pass HouseAdsNativeView or a View in setNativeAdView()");

            }
            if (dialogModal.getIconUrl().trim().isEmpty() || !dialogModal.getIconUrl().trim().contains("http"))
                throw new IllegalArgumentException("Icon URL should not be Null or Blank & should start with \"http\"");
            if (!dialogModal.getLargeImageUrl().trim().isEmpty() && !dialogModal.getLargeImageUrl().trim().contains("http"))
                throw new IllegalArgumentException("Header Image URL should start with \"http\"");
            if (dialogModal.getAppTitle().trim().isEmpty() || dialogModal.getAppDesc().trim().isEmpty())
                throw new IllegalArgumentException("Title & description should not be Null or Blank.");


            Picasso.get().load(dialogModal.getIconUrl()).into(icon, new Callback() {
                @Override
                public void onSuccess() {
                    if (usePalette) {
                        Palette palette = Palette.from(((BitmapDrawable) (icon.getDrawable())).getBitmap()).generate();
                        int dominantColor = palette.getDominantColor(ContextCompat.getColor(mContext, R.color.colorAccent));

                        if (cta.getBackground() instanceof ColorDrawable) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) cta.setBackground(new GradientDrawable());
                            else cta.setBackgroundDrawable(new GradientDrawable());
                        }
                        GradientDrawable drawable = (GradientDrawable) cta.getBackground();
                        drawable.setColor(dominantColor);

                        if (dialogModal.getRating() > 0) {
                            ratings.setRating(dialogModal.getRating());
                            Drawable ratingsDrawable = ratings.getProgressDrawable();
                            DrawableCompat.setTint(ratingsDrawable, dominantColor);
                        } else ratings.setVisibility(View.GONE);
                    }


                    if (dialogModal.getLargeImageUrl().trim().isEmpty()) {
                        isAdLoaded = true;
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoaded();
                    }
                }

                @Override
                public void onError(Exception e) {
                    isAdLoaded = false;
                    if (headerImage == null || dialogModal.getLargeImageUrl().isEmpty()) {
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoadFailed(e);
                    }
                }
            });


            if (!dialogModal.getLargeImageUrl().trim().isEmpty())
                Picasso.get().load(dialogModal.getLargeImageUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if (headerImage != null) {
                            headerImage.setVisibility(View.VISIBLE);
                            headerImage.setImageBitmap(bitmap);
                        }
                        isAdLoaded = true;
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoaded();
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        if (mNativeAdListener != null) mNativeAdListener.onAdLoadFailed(e);
                        isAdLoaded = false;
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            else {
                if (headerImage != null) headerImage.setVisibility(View.GONE);
            }

            title.setText(dialogModal.getAppTitle());
            description.setText(dialogModal.getAppDesc());
            if (price != null) {
                price.setVisibility(View.VISIBLE);
                if (!dialogModal.getPrice().trim().isEmpty()) price.setText(String.format("Price: %s", dialogModal.getPrice()));
                else price.setVisibility(View.GONE);
            }

            if (ratings != null ) {
                ratings.setVisibility(View.VISIBLE);
                if (dialogModal.getRating() > 0) ratings.setRating(dialogModal.getRating());
                else ratings.setVisibility(View.GONE);
            }

            if (cta != null) {
                if (cta instanceof TextView) ((TextView) cta).setText(dialogModal.getCtaText());
                if (cta instanceof Button) ((Button) cta).setText(dialogModal.getCtaText());
                if (!(cta instanceof TextView))
                    throw new IllegalArgumentException("Call to Action View must be either a Button or a TextView");

                cta.setOnClickListener(view -> {
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
                });
            }
        }

    }
}
