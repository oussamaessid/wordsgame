package app.wordgame

import android.os.Bundle
import app.wordgame.BuildConfig
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _root_ide_package_.app.wordgame.di.AppContainer.initialize(this)

        // BuildConfig.DEBUG active automatiquement les annonces de test en développement
        app.wordgame.ads.AdManager.initialize(this, debugMode = BuildConfig.DEBUG)

        app.wordgame.ads.AdManager.loadAppOpenAd(this)
        app.wordgame.ads.AdManager.loadInterstitial(this)
        app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this)
        app.wordgame.ads.AdManager.loadRewardedAdSolution(this)
        app.wordgame.ads.AdManager.loadInterstitialRewardFallback(this)

        appStartTime = System.currentTimeMillis()

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        setContent {
            var showAppOpenAd by remember { mutableStateOf(true) }
            var appOpenAdShown by remember { mutableStateOf(false) }

            // Délai de 2.5s pour éviter les clics accidentels au lancement
            LaunchedEffect(Unit) {
                delay(2500)
                if (!appOpenAdShown) {
                    app.wordgame.ads.AdManager.showAppOpenAd(this@MainActivity) {
                        showAppOpenAd = false
                        appOpenAdShown = true
                    }
                } else {
                    showAppOpenAd = false
                }
            }

            // SUPPRIMÉ : boucle automatique d'interstitielle toutes les 5 min.
            // Les interstitielles ne doivent s'afficher que sur action utilisateur
            // (fin de partie, navigation) pour éviter le trafic invalide.

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
        app.wordgame.ads.AdManager.loadInterstitial(this)
        app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this)
        app.wordgame.ads.AdManager.loadRewardedAdSolution(this)
        app.wordgame.ads.AdManager.loadInterstitialRewardFallback(this)
    }
}