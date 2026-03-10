package com.vahitkeskin.fencecalculator.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.vahitkeskin.fencecalculator.BuildConfig

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    // ID local.properties dosyasından çekiliyor
    val bannerAdId = BuildConfig.ADMOB_BANNER_ID
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Adaptive Banner Boyutu
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        androidx.compose.ui.platform.LocalContext.current,
        screenWidth
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(adSize)
                    adUnitId = bannerAdId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("BannerAd", "Banner reklam başarıyla yüklendi")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("BannerAd", "Banner reklam yüklenemedi: ${error.message}, Kod: ${error.code}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
