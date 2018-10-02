# HouseAds
A simple Android library (currently in early stage) to cross promote your apps, sites!
<br/>Currently includes a Dialog & Interstitial Ad fetched from a json stored on a site/server.


Primary Goal:
<br/>To keep it Simple & including Ads support like AdMob's Ad (Native, Interstitial)
<br/>No!, No Banners!

## Gradle
[![Download](https://api.bintray.com/packages/itznotabug/Maven/houseAds/images/download.svg) ](https://bintray.com/itznotabug/Maven/houseAds/_latestVersion)
<br/>Adding HouseAds in your App - 
```gradle
dependencies {
    implementation 'com.lazygeniouz:houseAds:1.2'
}
```

## Json Array Schema
Json Array Schema that you'll have to put on a server:
```json 
   {
    "apps":

     [{
       "app_title": "App Name (Dialog)",
       "app_desc": "Your App's description",
       "app_icon": "https:// URL to Icon",
       "app_header_image": "https:// URL to Header Image",
       "app_uri": "http:// URL or Package Name - com.package.name",
       "app_rating": "4.5",
       "app_cta_text": "Install",
       "app_price": "Free",
       "app_adType": "dialog"
     },

     {
       "app_title": "App Name 2 (Interstitial)",
       "app_desc": "Your App's Description",
       "app_icon": "https:// URL to Icon",
       "app_header_image": "https:// URL to Header Image",
       "app_uri": "http:// URL or Package Name - com.package.name",
       "app_rating": "4.5",
       "app_cta_text": "Install",
       "app_price": "Free",
       "app_adType": "interstitial"
     },
      
     {
     "app_title": "App Name 3 (Native Ad)",
     "app_desc": "Your App's Description",
     "app_icon": "https:// URL to Icon",
     "app_header_image": "https:// URL to Header Image",
     "app_uri": "http:// URL or Package Name - com.package.name",
     "app_rating": "4.5",
     "app_cta_text": "Install",
     "app_price": "Free",
     "app_adType": "native"
     }]
    }
```


<br/>Some of the Assets like App Title, App Description, Icons & call to Action Text & Package Name are necessary!
<!-- <br/>Code Examples will be added later, till then you can check Sample App!-->

## HouseAdsDialog
HouseAdsDialog is a Beautifully Styled Dialog which shows your Ad Assets like Header Image, App Icon, App Title & Description, Call to Action Button, Star Ratings & Price of the App.
<br/>The library internally uses `Palette API` to color the CTA Button by fetching the `Dominant Color` from Icon or Header Bitmap, whichever available.

<br/>Following is an example of HouseAdsDialog -     
```java
HouseAdsDialog houseAds = new HouseAdsDialog(MainActivity.this);
houseAds.setUrl(adURL); //URL to Json File
houseAds.hideIfAppInstalled(true); //An App's Ad won't be shown if it is Installed on the Device.
houseAds.setCardCorners(100); // Set CardView's corner radius.
houseAds.setCtaCorner(100); //Set CTA Button's background radius.
houseAds.setForceLoadFresh(false); //Fetch Json everytime loadAds() is called, true by default, if set to false, Json File's Response is kept untill App is closed! 
houseAds.showHeaderIfAvailable(false); //Show Header Image if available, true by default
houseAds.loadAds();
```
             
<br/>You can check if the Ad is loaded via - 
```java
houseAds.isAdLoaded(); 
//returns true if loaded, false otherwise!
```
    
<br/>You can also add a Listener to HouseAdsDialog,
```java
houseAds.setAdListener(new AdListener() {
    @Override
    public void onAdLoadFailed() {}
    
    @Override
    public void onAdLoaded() {
        //Show AdDialog as soon as it is loaded.
        houseAds.showAd();
    }
             
    @Override
    public void onAdClosed() {}
    
    @Override
    public void onAdShown() {}
     
    @Override
    public void onApplicationLeft() {}
});
```

**<br/>NOTE: You cannot Customize the Dialog except for the CardView's Corner Radius & CTA Button's Background Radius!**
<br/>Use `HouseAdsNative` instead :)

## HouseAdsInterstitial
HouseAds also supports Interstitial Ad support just like AdMob has one!
<br/>HouseAdsInterstitial shows an Image fetched from your Json & navigates the User to Google Play if you specified a Package Name or the Website otherwise.

<br/>Following is an example of HouseAdsInterstitial - 
```java
final HouseAdsInterstitial interstitial = new HouseAdsInterstitial(MainActivity.this);
interstitial.setUrl(adURL);
interstitial.setAdListener(new AdListener() {
    @Override
    public void onAdLoadFailed() {}
    
    @Override
    public void onAdLoaded() {}
     
    @Override
    public void onAdClosed() {}
     
    @Override
    public void onAdShown() {}
     
    @Override
    public void onApplicationLeft() {}
});
interstitial.loadAd();
```
            
Just like the HouseAdsDialog, you can check if the Interstitial is Loaded in the same way - `interstitial.isAdLoaded();`

And show Interstitial like - `interstitial.show();`

## HouseAdsNative
HouseAdsNative is the type of Ad where you can define your own layouts for the Ad Assets just like AdMob's `NativeAdvancedUnified`.
<br/>You'll need to pass the ids of the Assets (Icon, Call to Action View, Header Image etc) in a `HouseAdsNativeView` in their respective setter methods
and then set that `NativeView` object to the HouseAdsNative's `setNativeView()` .
<br/>Following is an example of `HouseAdsNativeView` - 
```java
final Relativelayout adLayout = findViewById(R.id.adLayout); //Ad Assets inside a ViewGroup
adlayout.setVisibility(View.GONE):
```
```java
HouseAdsNativeView nativeView = new HouseAdsNativeView();
nativeView.setTitleView((TextView) findViewById(R.id.appinstall_headline));
nativeView.setDescriptionView((TextView) findViewById(R.id.appinstall_body));
nativeView.setIconView((ImageView) findViewById(R.id.appinstall_app_icon));
nativeView.setHeaderImageView((ImageView) findViewById(R.id.large));
nativeView.setCallToActionView(findViewById(R.id.appinstall_call_to_action));
nativeView.setPriceView((TextView) findViewById(R.id.price));
nativeView.setRatingsView((RatingBar) findViewById(R.id.rating));
``` 
```java
HouseAdsNative houseAdsNative = new HouseAdsNative(NativeAdActivity.this);
houseAdsNative.setNativeAdView(nativeView);
houseAdsNative.setUrl(adUrl);
houseAdsNative.setNativeAdListener(new NativeAdListener() {            
    @Override
    public void onAdLoaded() {
        adLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdLoadFailed() {
        Toast.makeText(NativeAdActivity.this, "Failed", Toast.LENGTH_SHORT).show();
    }
});
houseAdsNative.loadAds();
```
<br/>Check if NativeAd is loaded - `houseAdsNative.isAdLoaded();`
<br/>Additionally, you can define your own 'Call to Action' Button's action by using a `CallToActionListener`, for e.g.
```java
houseAdsNative.setCallToActionListener(new NativeAdListener.CallToActionListener() {
            @Override
            public void onCallToActionClicked(View view) {
                //Do your Stuff!
            }
        });
```
<br/>**Note: If you don't implement the CTAListener, default implementation is used which navigates the user to PlayStore or Website depending on the passed argument to the "app_uri" object in json, when clicked.**


## Clearing the Cache.
HouseAds uses Glide for Image Loading and Caching, therefore you should clear its cache periodically by calling the following method - 
```java
HouseAdsHelper.clearGlideCache(MainActivity.this);
```

## ToDo:
* Add AdsActivity (Recommendations Activity) with RecyclerView.
* Add a setView(View view) method in HouseAdsNative.
* Add Sample App Screenshots.
* <strike>Add the NativeHouseAd Support.</strike> ✔
* <strike>Add Library to JCenter();</strike> ✔

 
 
 

 
 
 
 
 
 
             
