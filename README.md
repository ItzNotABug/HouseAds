# HouseAds
A simple Android library (currently in very, very early stage) to cross promote your apps, sites!
<br/>Currently includes a Dialog & Interstitial Ad fetched from a json stored on a site/server.


Primary Goal:
<br/>To keep it Simple & including Ads support like AdMob's Ad (Native, Interstitial)
<br/>No!, No Banners!

<br/>Json Array Schema that you'll have to put on a server:
```json 
   {
    "apps":

     [{
       "app_title": "App Name",
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
       "app_title": "App Name 2",
       "app_desc": "Your App's Description",
       "app_icon": "https:// URL to Icon",
       "app_header_image": "https:// URL to Header Image",
       "app_uri": "http:// URL or Package Name - com.package.name",
       "app_rating": "4.5",
       "app_cta_text": "Install",
       "app_price": "Free",
       "app_adType": "interstitial"
     }]
     }
```


<br/>Some of the Assets like App Title, App Description, Icons & call to Action Text & Package Name are necessary!
<!-- <br/>Code Examples will be added later, till then you can check Sample App!-->

# HouseAdsDialog
<br/>HouseAdsDialog is a Beautifully Styled Dialog which shows your Ad Assets like Header Image, App Icon, App Title & Description, Call to Action Button, Star Ratings & Price of the App.
<br/>The library internally uses Palette API to color the CTA Button by fetching the `Dominant Color` from Icon Bitmap.

<br/>Following is an example of HouseAdsDialog -     
```java
HouseAdsDialog houseAds = new HouseAdsDialog(MainActivity.this);
houseAds.setUrl(adURL); //URL to Json File
houseAds.hideIfAppInstalled(true); //An App's Ad won't be shown if ot is Installed on the Device.
houseAds.setCardCorners(100); // Set CardView's corner radius.
houseAds.setCtaCorner(100); //Set CTA Button's background radius.
houseAds.setForceLoadFresh(false); //Fetch Json everytime loadAds() is called, true by default, if set to false, Json File's Response is kept untill App is closed! 
houseAds.showHeaderIfAvailable(false); //Show Header Image if available, true by default
houseAds.loadAds();
```
             
<br/>You can check if the Ad is loaded via - 
```java
houseAds.isAddLoaded(); returns true if Loaded, false otherwise!
```
    
<br/>You can also add a Listener to HouseAdsDialog,
```java
houseAds.addListener(new AdListener() {
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
<br/>A `NativeHouseAd` implementation will be added too, where you will have FULL CONTROL of the Layout! :)

# HouseAdsInterstitial
<br/>HouseAds also supports Interstitial Ad support just like AdMob has one!
<br/>HouseAdsInterstitial shows an Image fetched from your Json & navigates the User to Google Play if you specified a Package Name or the Website otherwise.

<br/>Following is an example of HouseAdsInterstitial - 
```java
final HouseAdsInterstitial interstitial = new HouseAdsInterstitial(MainActivity.this);
interstitial.setUrl(adURL);
interstitial.addListener(new AdListener() {
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
            
Just like the HouseAdsDialog, you can check if the Interstitial is Loaded in the same way - 
```java 
interstitial.isAdLoaded();
```

And show Interstitial like - 
```java    
interstitial.show();
```

# ToDo:
* Add Sample App Screenshots.
* Add the NativeHouseAd Support.

 
 
 

 
 
 
 
 
 
             
