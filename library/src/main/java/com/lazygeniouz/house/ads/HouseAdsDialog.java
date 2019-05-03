package com.lazygeniouz.house.ads;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;

import com.lazygeniouz.house.ads.helper.HouseAdsHelper;
import com.lazygeniouz.house.ads.helper.JsonPullerTask;
import com.lazygeniouz.house.ads.helper.RemoveJsonObjectCompat;
import com.lazygeniouz.house.ads.listener.AdListener;
import com.lazygeniouz.house.ads.modal.DialogModal;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HouseAdsDialog {
    private final Context mCompatActivity;
    private final String jsonUrl;
    private String jsonRawResponse = "";

    private boolean showHeader = true;
    private boolean forceLoadFresh = true;
    private boolean hideIfAppInstalled = true;
    private boolean usePalette = true;
    private int cardCorner = 25;
    private int ctaCorner = 25;
    private static boolean isAdLoaded = false;

    private AdListener mAdListener;
    private AlertDialog dialog;

    private static int lastLoaded = 0;

    public HouseAdsDialog(Context context, String url) {
        this.mCompatActivity = context;
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

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
    }

    public void loadAds() {
        isAdLoaded = false;
        if (jsonUrl.trim().isEmpty()) throw new IllegalArgumentException("Url is Blank!");
        else {
            if (forceLoadFresh || jsonRawResponse.isEmpty())
                new JsonPullerTask(jsonUrl, result -> {
                    if (!result.trim().isEmpty()) {
                        jsonRawResponse = result;
                        setUp(result);
                    } else {
                        if (mAdListener != null)
                            mAdListener.onAdLoadFailed(new Exception("Null Response"));
                    }
                }).execute();

            if (!forceLoadFresh && !jsonRawResponse.trim().isEmpty()) setUp(jsonRawResponse);
        }
    }

    public void showAd() {
        if (dialog != null) dialog.show();
    }

    private void setUp(String response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCompatActivity);
        ArrayList<DialogModal> val = new ArrayList<>();

        try {
            JSONObject rootObject = new JSONObject(response);
            JSONArray array = rootObject.optJSONArray("apps");

            for (int object = 0; object < array.length(); object++) {
                final JSONObject jsonObject = array.getJSONObject(object);


                if (hideIfAppInstalled && !jsonObject.optString("app_uri").startsWith("http") && HouseAdsHelper.isAppInstalled(mCompatActivity, jsonObject.optString("app_uri")))
                    new RemoveJsonObjectCompat(object, array).execute();
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

            final View view = View.inflate(mCompatActivity, R.layout.house_ads_dialog_layout, null);

            if (dialogModal.getIconUrl().trim().isEmpty() || !dialogModal.getIconUrl().trim().startsWith("http"))
                throw new IllegalArgumentException("Icon URL should not be Null or Blank & should start with \"http\"");
            if (!dialogModal.getLargeImageUrl().trim().isEmpty() && !dialogModal.getLargeImageUrl().trim().startsWith("http"))
                throw new IllegalArgumentException("Header Image URL should start with \"http\"");
            if (dialogModal.getAppTitle().trim().isEmpty() || dialogModal.getAppDesc().trim().isEmpty())
                throw new IllegalArgumentException("Title & description should not be Null or Blank.");


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


            Picasso.get().load(dialogModal.getIconUrl()).into(icon, new Callback() {
                @Override
                public void onSuccess() {
                    isAdLoaded = true;
                    if (mAdListener != null) mAdListener.onAdLoaded();

                    if (icon.getVisibility() == View.GONE) icon.setVisibility(View.VISIBLE);
                    int dominantColor = ContextCompat.getColor(mCompatActivity, R.color.colorAccent);
                    if (usePalette) {
                        Palette palette = Palette.from(((BitmapDrawable) (icon.getDrawable())).getBitmap()).generate();
                        dominantColor = palette.getDominantColor(ContextCompat.getColor(mCompatActivity, R.color.colorAccent));
                    }

                    GradientDrawable drawable = (GradientDrawable) cta.getBackground();
                    drawable.setColor(dominantColor);

                    if (dialogModal.getRating() > 0) {
                        ratings.setRating(dialogModal.getRating());
                        Drawable ratingsDrawable = ratings.getProgressDrawable();
                        DrawableCompat.setTint(ratingsDrawable, dominantColor);
                    } else ratings.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    isAdLoaded = false;
                    if (mAdListener != null) mAdListener.onAdLoadFailed(e);
                    icon.setVisibility(View.GONE);
                }
            });

            if (!dialogModal.getLargeImageUrl().trim().isEmpty() && showHeader) {
                Picasso.get().load(dialogModal.getLargeImageUrl()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        headerImage.setImageBitmap(bitmap);
                        headerImage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        headerImage.setVisibility(View.GONE);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            } else headerImage.setVisibility(View.GONE);


            title.setText(dialogModal.getAppTitle());
            description.setText(dialogModal.getAppDesc());
            cta.setText(dialogModal.getCtaText());
            if (dialogModal.getPrice().trim().isEmpty()) price.setVisibility(View.GONE);
            else price.setText(String.format("Price: %s", dialogModal.getPrice()));


            builder.setView(view);
            dialog = builder.create();
            //noinspection ConstantConditions
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setOnShowListener(dialogInterface -> {
                if (mAdListener != null) mAdListener.onAdShown();
            });
            dialog.setOnCancelListener(dialogInterface -> {
                if (mAdListener != null) mAdListener.onAdClosed();
            });
            dialog.setOnDismissListener(dialogInterface -> {
                if (mAdListener != null) mAdListener.onAdClosed();
            });

            cta.setOnClickListener(view1 -> {
                dialog.dismiss();

                String packageOrUrl = dialogModal.getPackageOrUrl();
                if (packageOrUrl.trim().startsWith("http")) {
                    mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(packageOrUrl)));
                    if (mAdListener != null) mAdListener.onApplicationLeft();
                } else {
                    try {
                        mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageOrUrl)));
                        if (mAdListener != null) mAdListener.onApplicationLeft();
                    } catch (ActivityNotFoundException e) {
                        if (mAdListener != null) mAdListener.onApplicationLeft();
                        mCompatActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageOrUrl)));
                    }
                }
            });
        }

    }
}




