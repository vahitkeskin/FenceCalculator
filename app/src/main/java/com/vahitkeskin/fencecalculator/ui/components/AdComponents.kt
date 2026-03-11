package com.vahitkeskin.fencecalculator.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var isAdLoaded by remember { androidx.compose.runtime.mutableStateOf(false) }
    
    // Adaptive Banner Boyutu
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        context,
        configuration.screenWidthDp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder UI: Shown until ad is loaded
        if (!isAdLoaded) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reklam Yükleniyor...",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { factoryContext ->
                AdView(factoryContext).apply {
                    setAdSize(adSize)
                    adUnitId = bannerAdId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            isAdLoaded = true
                            Log.d("BannerAd", "Banner reklam başarıyla yüklendi")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            // On failure, we might keep the placeholder or hide it, 
                            // but here we mark as loaded to potentially show error logs or hide space if needed.
                            // However, user specifically asked for placeholder until loaded.
                            Log.e("BannerAd", "Banner reklam yüklenemedi: ${error.message}, Kod: ${error.code}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
