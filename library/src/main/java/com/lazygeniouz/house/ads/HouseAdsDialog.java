/*
 * Created by Darshan Pandya.
 * @itznotabug
 * Copyright (c) 2018.
 */

package com.lazygeniouz.house.ads;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.lazygeniouz.house.ads.helper.HouseAdsHelper;
import com.lazygeniouz.house.ads.helper.JsonPullerTask;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.modal.DialogModal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HouseAdsDialog {
    private final Context mCompatActivity;
    private String jsonUrl;
    private String jsonRawResponse = "";

    private boolean showHeader = true;
    private boolean forceLoadFresh = true;
    private boolean hideIfAppInstalled  = false;
    private int cardCorner = 25;
    private int ctaCorner = 25;
    private static boolean isAdLoaded = false;

    private AdListener mAdListener;
    private AlertDialog dialog;

    private static int lastLoaded = 0;

    public HouseAdsDialog(Context context) {
        this.mCompatActivity = context;
    }

    public void setUrl(String url) {
        this.jsonUrl = url;
    }

    public void showHeaderIfAvailable(boolean val) {
        this.showHeader = val;
    }

    public void setCardCorners(int corners) {
        this.cardCorner = corners;
    }

    public void setCtaCorner(int corner) {
        this.ctaCorner = corner;
    }

    public void setForceLoadFresh(boolean val) {
        this.forceLoadFresh = val;
    }

    public void setAdListener(AdListener listener) {
        this.mAdListener = listener;
    }

    @SuppressWarnings("unused")
    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    public void hideIfAppInstalled(boolean val) {
        this.hideIfAppInstalled = val;
    }

    public void loadAds() {
        isAdLoaded = false;
        if (jsonUrl.trim().equals("")) throw new IllegalArgumentException("Url is Blank!");
        else {
            if (forceLoadFresh || jsonRawResponse.equals("")) new JsonPullerTask(jsonUrl, new JsonPullerTask.JsonPullerListener() {
                @Override
                public void onPostExecute(String result) {
                    if (!result.trim().equals("")) {
                        jsonRawResponse = result;
                        setUp(result);
                    }
                    else {
                        if (mAdListener != null) mAdListener.onAdLoadFailed();
                    }
                }
            }).execute();
            if (!forceLoadFresh && !jsonRawResponse.trim().equals("")) setUp(jsonRawResponse);
        }
    }

    public void showAd() {
        if (dialog != null) dialog.show();
    }

    @SuppressLint("NewApi")
    private void setUp(String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCompatActivity);
        ArrayList<DialogModal> val = new ArrayList<>();

        try {
            JSONObject rootObject = new JSONObject(response);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                final JSONObject jsonObject = array.getJSONObject(object);


                if (hideIfAppInstalled && !jsonObject.optString("app_uri").startsWith("http") &&  HouseAdsHelper.isAppInstalled(mCompatActivity, jsonObject.optString("app_uri"))) array.remove(object);
                //ToDo: Handle remove() on pre 19!
                else {
                    //We Only Add Dialog Ones!
                    if (jsonObject.optString("app_adType").equals("dialog")) {
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


            @SuppressLint("InflateParams") final View view = LayoutInflater.from(mCompatActivity).inflate(R.layout.dialog, null);

            if (dialogModal.getIconUrl().trim().equals("") || !dialogModal.getIconUrl().trim().contains("http")) throw new IllegalArgumentException("Icon URL should not be Null or Blank & should start with \"http\"");
            if (!dialogModal.getLargeImageUrl().trim().equals("") && !dialogModal.getIconUrl().trim().contains("http")) throw new IllegalArgumentException("Header Image URL should start with \"http\"");
            if (dialogModal.getAppTitle().trim().equals("") || dialogModal.getAppDesc().trim().equals("")) throw new IllegalArgumentException("Title & description should not be Null or Blank.");


            CardView cardView = view.findViewById(R.id.houseAds_card_view);
            cardView.setRadius(cardCorner);

            final Button cta = view.findViewById(R.id.houseAds_cta);
            GradientDrawable gd = (GradientDrawable) cta.getBackground();
            gd.setCornerRadius(ctaCorner);

            final ImageView icon = view.findViewById(R.id.houseAds_app_icon);
            final ImageView headerImage = view.findViewById(R.id.houseAds_header_image);
            TextView title = view.findViewById(R.id.houseAds_title);
            TextView description = view.findViewById(R.id.houseAds_description);
            final RatingBar ratings = view.findViewById(R.id.houseAds_rating);
            TextView price = view.findViewById(R.id.houseAds_price);


            Glide.with(mCompatActivity).load(dialogModal.getIconUrl()).asBitmap().into(new SimpleTarget<Bitmap>(Integer.MIN_VALUE, Integer.MIN_VALUE) {
                @Override
                public void onResourceReady(@NonNull Bitmap glideBitmap, GlideAnimation<? super Bitmap> p2) {
                    icon.setImageBitmap(glideBitmap);

                    Palette palette = Palette.from(glideBitmap).generate();
                    int dominantColor = palette.getDominantColor(ContextCompat.getColor(mCompatActivity, R.color.colorAccent));

                    if (!showHeader) {
                        isAdLoaded = true;
                        if (mAdListener != null) mAdListener.onAdLoaded();
                    }
                    GradientDrawable drawable = (GradientDrawable)  cta.getBackground();
                    drawable.setColor(dominantColor);

                    if (dialogModal.getRating() != 0) {
                        ratings.setRating(dialogModal.getRating());
                        Drawable ratingsDrawable = ratings.getProgressDrawable();
                        DrawableCompat.setTint(ratingsDrawable, dominantColor);
                    } else ratings.setVisibility(View.GONE);
                }});

            if (!dialogModal.getLargeImageUrl().trim().equals("") && showHeader) headerImage.setVisibility(View.VISIBLE);
            Glide.with(mCompatActivity).load(dialogModal.getLargeImageUrl()).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable GlideAnimation<? super Bitmap> transition) {
                    if (showHeader) {
                        isAdLoaded = true;
                        if (mAdListener != null) mAdListener.onAdLoaded();
                    }
                    headerImage.setImageBitmap(bitmap);
                }
            });

            title.setText(dialogModal.getAppTitle());
            description.setText(dialogModal.getAppDesc());
            cta.setText(dialogModal.getCtaText());
            if (dialogModal.getPrice().trim().equals("")) price.setVisibility(View.GONE);
            else price.setText(String.format("Price: %s", dialogModal.getPrice()));


            builder.setView(view);
            dialog = builder.create();
            //noinspection ConstantConditions
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    if (mAdListener != null) mAdListener.onAdShown();
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (mAdListener != null) mAdListener.onAdClosed();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if (mAdListener != null) mAdListener.onAdClosed();
                }
            });

            cta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();

                    String packageOrUrl = dialogModal.getPackageOrUrl();
                    if (packageOrUrl.trim().startsWith("http")) {
                        mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)));
                        if (mAdListener != null) mAdListener.onApplicationLeft();
                    }
                    else {
                        try {
                            mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageOrUrl)));
                            if (mAdListener != null) mAdListener.onApplicationLeft();
                        } catch (ActivityNotFoundException e) {
                            if (mAdListener != null) mAdListener.onApplicationLeft();
                            mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageOrUrl)));
                        }
                    }
                }
            });
        }

    }

}




