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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.modal.DialogModal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HouseAdsDialog {
    private Context mCompatActivity;
    private String jsonUrl;
    private String jsonRawResponse = "";

    private boolean showHeader = true;
    private boolean forceLoadFresh = true;
    private static boolean isAdLoaded = false;

    private AdListener mAdListener;

    private int lastLoaded = 0;

    public HouseAdsDialog(Context context) {
        this.mCompatActivity = context;
    }

    public void setUrl(String url) {
        this.jsonUrl = url;
    }

    public void showHeaderIfAvailable(boolean val) {
        this.showHeader = val;
    }

    public void setForceLoadFresh(boolean val) {
        this.forceLoadFresh = val;
    }

    public void addListener(AdListener listener) {
        this.mAdListener = listener;
    }

    public boolean isAdLoaded() {
        return isAdLoaded;
    }

    public void loadAds() {
        isAdLoaded = false;
        if (jsonUrl.trim().equals("")) throw new IllegalArgumentException("Url is Blank!");
        else {
            if (jsonRawResponse.equals("")) new ScanUrlTask(jsonUrl).execute();
            else {
                if (!forceLoadFresh) showAd();
                else new ScanUrlTask(jsonUrl).execute();
            }
        }
    }

    public void showAd() {
        ArrayList<DialogModal> val = new ArrayList<>();

        String x = new String(new StringBuilder().append(jsonRawResponse));

        try {
            JSONObject rootObject = new JSONObject(x);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                final JSONObject jsonObject = array.getJSONObject(object);

                final DialogModal dialogModal = new DialogModal();
                dialogModal.setAppTitle(jsonObject.optString("app_title"));
                dialogModal.setAppDesc(jsonObject.optString("app_desc"));
                dialogModal.setIconUrl(jsonObject.optString("app_icon"));
                dialogModal.setLargeImageUrl(jsonObject.optString("app_header_image"));
                dialogModal.setCtaText(jsonObject.optString("app_cta_text"));
                dialogModal.setPackageName(jsonObject.optString("app_package"));
                dialogModal.setRating(jsonObject.optInt("app_rating"));
                dialogModal.setPrice(jsonObject.optString("app_price"));

                val.add(dialogModal);
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

            final ImageView icon = view.findViewById(R.id.appinstall_app_icon);
            final ImageView headerImage = view.findViewById(R.id.large);
            TextView title = view.findViewById(R.id.appinstall_headline);
            TextView description = view.findViewById(R.id.appinstall_body);
            Button cta = view.findViewById(R.id.appinstall_call_to_action);
            final RatingBar ratings = view.findViewById(R.id.rating);
            TextView price = view.findViewById(R.id.price);


            Glide.with(mCompatActivity).asBitmap().load(dialogModal.getIconUrl()).into(new SimpleTarget<Bitmap>(Integer.MIN_VALUE, Integer.MIN_VALUE) {
                @Override
                public void onResourceReady(@NonNull Bitmap glideBitmap, Transition<? super Bitmap> p2) {
                    if (!showHeader) {
                        isAdLoaded = true;
                        if (mAdListener != null) mAdListener.onAdLoaded();
                    }
                    icon.setImageBitmap(glideBitmap);

                    if (dialogModal.getRating() != 0) {
                        ratings.setRating(dialogModal.getRating());
                        Palette palette = Palette.from(glideBitmap).generate();
                        Drawable drawable = ratings.getProgressDrawable();
                        drawable.setColorFilter(palette.getDominantColor(ContextCompat.getColor(mCompatActivity, R.color.colorAccent)), PorterDuff.Mode.SRC_ATOP);
                    }
                    else view.findViewById(R.id.rating).setVisibility(View.GONE);

                    GradientDrawable drawable = (GradientDrawable)  view.findViewById(R.id.appinstall_call_to_action).getBackground();
                    Palette palette = Palette.from(glideBitmap).generate();
                    drawable.setColor(palette.getDominantColor(ContextCompat.getColor(mCompatActivity, R.color.colorAccent)));
                }});

            if (!dialogModal.getLargeImageUrl().trim().equals("") && showHeader) view.findViewById(R.id.large).setVisibility(View.VISIBLE);
            Glide.with(mCompatActivity).asBitmap().apply(new RequestOptions().override(headerImage.getWidth(), 175)).load(dialogModal.getLargeImageUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                    if (showHeader) isAdLoaded = true;
                    if (mAdListener != null) mAdListener.onAdLoaded();
                    headerImage.setImageBitmap(bitmap);
                }
            });

            title.setText(dialogModal.getAppTitle());
            description.setText(dialogModal.getAppDesc());
            cta.setText(dialogModal.getCtaText());
            if (dialogModal.getPrice().trim().equals("")) price.setVisibility(View.GONE);
            else price.setText(String.format("Price: %s", dialogModal.getPrice()));


            AlertDialog.Builder builder = new AlertDialog.Builder(mCompatActivity).setView(view);
            final AlertDialog dialog = builder.create();
            //noinspection ConstantConditions
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
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
                    if (mAdListener != null) mAdListener.onApplicationLeft();
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + dialogModal.getPackageName()));
                    try {
                        mCompatActivity.startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + dialogModal.getPackageName())));
                    }
                }
            });
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class ScanUrlTask extends AsyncTask<String, String, String> {
        String url;

        ScanUrlTask(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... p1) {
                return Helper.parseJsonObject(url);
        }

        @Override
        protected void onPostExecute(String result) {
            jsonRawResponse = result;
            //showAd(result);
        }
    }

}




