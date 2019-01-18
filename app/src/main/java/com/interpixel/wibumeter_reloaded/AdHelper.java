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
    private static final String INTERSTITIAL_ID = "";
    private static final String REWARDED_ID = "";

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
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int errorCode) {
                if(errorCode != 1){
                    SetupInterstitialAd();
                }
            }
        });
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void SetupRewardedAd(){
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if(i != 1){
                    SetupRewardedAd();
                }
            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });
        rewardedVideoAd.loadAd(REWARDED_ID, new AdRequest.Builder().build());
    }
}
