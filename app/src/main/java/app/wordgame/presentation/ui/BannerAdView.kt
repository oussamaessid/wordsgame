package app.wordgame.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    isLanguageScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = app.wordgame.ads.AdManager.getBannerAdId(isLanguageScreen)
                    setAdSize(AdSize.BANNER)
                    adListener = object : AdListener() {
                        override fun onAdClicked() {
                            // Enregistre le clic pour activer le cooldown anti-clic invalide
                            app.wordgame.ads.AdManager.recordAdClick()
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            android.util.Log.e("BannerAdView", "Failed to load banner: ${error.message}")
                        }
                    }
                    loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )
    }
}