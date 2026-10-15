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
    // Mise à jour obligatoire : bloque l'app tant qu'une version plus récente existe sur le Play Store.
    private val forceUpdate = ForceUpdateManager(this)

    private var appStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceUpdate.check { }

        // BuildConfig.DEBUG active automatiquement les annonces de test en développement
        app.wordgame.ads.AdManager.initialize(this, debugMode = BuildConfig.DEBUG)

        app.wordgame.ads.AdManager.loadAppOpenAd(this)
        app.wordgame.ads.AdManager.loadInterstitial(this)
        app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this)
        app.wordgame.ads.AdManager.loadRewardedAdSolution(this)

        appStartTime = System.currentTimeMillis()

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        setContent {
            // rememberSaveable : pas de nouvelle App Open après une rotation d'écran.
            var showAppOpenAd by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
            var appOpenAdShown by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

            // Délai de 2.5s pour éviter les clics accidentels au lancement
            LaunchedEffect(Unit) {
                if (!showAppOpenAd) return@LaunchedEffect
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
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!showAppOpenAd) {
                        app.wordgame.presentation.ui.MainAppScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        forceUpdate.resumeIfInProgress()
        app.wordgame.ads.AdManager.loadInterstitial(this)
        app.wordgame.ads.AdManager.loadRewardedAdExtraTry(this)
        app.wordgame.ads.AdManager.loadRewardedAdSolution(this)
    }
}