package com.interpixel.wibumeter_reloaded;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * Helper class for showing Interstitial ads and Reward video ads
 */
public class AdHelper {

    private static AdHelper adHelper;
    private Context context;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd rewardedVideoAd;
    private static final String INTERSTITIAL_ID = "ca-app-pub-6204912690888371/6401115418";
    private static final String REWARDED_ID = "ca-app-pub-6204912690888371/7998758807";

    private AdHelper(Context context){
        this.context = context;
    }

    public static void setAdHelper(Context context){
        adHelper = new AdHelper(context);
    }

    public static AdHelper getAdHelper(){
        return adHelper;
    }

    public void Initialize(){
        MobileAds.initialize(context, "ca-app-pub-6204912690888371~3472410188");
        SetupInterstitialAd();
        SetupRewardedAd();
    }

    public void ShowAds(){
        if(rewardedVideoAd.isLoaded()){
            rewardedVideoAd.show();
            SetupRewardedAd();
        }else if(interstitialAd.isLoaded()){
            interstitialAd.show();
            SetupInterstitialAd();
        }else{
            SetupInterstitialAd();
            SetupRewardedAd();
        }
    }

    private void SetupInterstitialAd(){
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(INTERSTITIAL_ID);
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void SetupRewardedAd(){
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.loadAd(REWARDED_ID, new AdRequest.Builder().build());
    }
}
