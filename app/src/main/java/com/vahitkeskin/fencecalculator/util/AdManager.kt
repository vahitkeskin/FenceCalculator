package com.vahitkeskin.fencecalculator.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.vahitkeskin.fencecalculator.BuildConfig

object AdManager {
    // ID local.properties dosyasından çekiliyor
    private val INTERSTITIAL_AD_ID = BuildConfig.ADMOB_INTERSTITIAL_ID

    private var interstitialAd: InterstitialAd? = null
    private var calculationClickCount = 0

    fun loadInterstitialAd(context: Context) {
        Log.d("AdManager", "Interstitial reklam yükleniyor ID: $INTERSTITIAL_AD_ID")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdManager", "Interstitial reklam yüklendi")
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Interstitial reklam yüklenemedi: ${error.message}")
                    interstitialAd = null
                }
            }
        )
    }

    fun onCalculateClicked(activity: Activity) {
        calculationClickCount++
        Log.d(
            "AdManager",
            "Hesapla tıklandı. Toplam hesaplama sayısı: ${calculationClickCount % 4}"
        )

        if (calculationClickCount % 3 == 0) {
            val adNumber = calculationClickCount / 3
            Log.d("AdManager", "$adNumber. reklam gösterilecek")
            showInterstitialAd(activity)
        }
    }

    private fun showInterstitialAd(activity: Activity) {
        interstitialAd?.let { ad ->
            Log.d("AdManager", "Interstitial reklam gösteriliyor...")
            ad.show(activity)
            // Load the next ad for future use
            loadInterstitialAd(activity)
        } ?: run {
            Log.d("AdManager", "Reklam henüz yüklü değil, tekrar yükleniyor...")
            // If ad is not loaded, try loading it again
            loadInterstitialAd(activity)
        }
    }
}
