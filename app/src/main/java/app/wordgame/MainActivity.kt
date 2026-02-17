package app.wordgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var appStartTime = 0L
    private var lastInterstitialCheck = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _root_ide_package_.app.wordgame.di.AppContainer.initialize(this)

        // Initialiser AdMob
        app.wordgame.ads.AdManager.initialize(this)

        // Charger TOUTES les annonces
        app.wordgame.ads.AdManager.loadAppOpenAd(this)
        app.wordgame.ads.AdManager.loadInterstitial(this)
        app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this)   // Charge aussi le fallback si échec
        app.wordgame.ads.AdManager.loadRewardedAdSolution(this)    // Charge aussi le fallback si échec

        // ✅ Charger le fallback interstitiel dès le démarrage (en plus des Rewarded)
        app.wordgame.ads.AdManager.loadInterstitialRewardFallback(this)

        appStartTime = System.currentTimeMillis()

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        setContent {
            var showAppOpenAd by remember { mutableStateOf(true) }
            var appOpenAdShown by remember { mutableStateOf(false) }

            // Afficher l'annonce d'ouverture une seule fois
            LaunchedEffect(Unit) {
                delay(500)
                if (showAppOpenAd && !appOpenAdShown) {
                    app.wordgame.ads.AdManager.showAppOpenAd(this@MainActivity) {
                        showAppOpenAd = false
                        appOpenAdShown = true
                    }
                } else {
                    showAppOpenAd = false
                }
            }

            // Vérifier périodiquement pour les annonces interstitielles
            LaunchedEffect(Unit) {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    while (true) {
                        delay(60000) // Vérifier chaque minute
                        val currentTime = System.currentTimeMillis()

                        if (currentTime - lastInterstitialCheck >= 5 * 60 * 1000) {
                            if (app.wordgame.ads.AdManager.showInterstitialIfReady(this@MainActivity)) {
                                lastInterstitialCheck = currentTime
                            }
                        }
                    }
                }
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!showAppOpenAd) {
                        _root_ide_package_.app.wordgame.presentation.ui.MainAppScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            app.wordgame.ads.AdManager.loadInterstitial(this@MainActivity)
            app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this@MainActivity)
            app.wordgame.ads.AdManager.loadRewardedAdSolution(this@MainActivity)
            app.wordgame.ads.AdManager.loadInterstitialRewardFallback(this@MainActivity)
        }
    }
}