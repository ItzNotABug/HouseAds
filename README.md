# HouseAds
A simple Android library (currently in very, very early stage) to cross promote your apps!
<br/>Currently includes a Dialog & Interstitial Ad fetched from a json which is stored on a site/server.


Primary Goal:
<br/>To keep it Simple & including Ads support like AdMob's Ad (Native, Interstitial, no Banners!)

<br/>Json Array Schema:
<br/>

    {
    "apps":

     [{
       "app_title": "App Name",
       "app_desc": "Your App's description",
       "app_icon": "https:// URL to Icon",
       "app_header_image": "https:// URL to Header Image",
       "app_package": "com.package.name",
       "app_rating": "4.5",
       "app_cta_text": "Install",
       "app_price": "Free"
     },

     {
       "app_title": "App Name 2",
       "app_desc": "Your App's Description",
       "app_icon": "https:// URL to Icon",
       "app_header_image": "https:// URL to Header Image",
       "app_package": "com.package.name",
       "app_rating": "4.5",
       "app_cta_text": "Install",
       "app_price": "Free"
     }]
     }


<br/>Some of the Assets like App Title, App Description, Icons & call to Action Text & Package Name are necessary!
<br/>Code Examples will be added later, till then you can check Sample App!