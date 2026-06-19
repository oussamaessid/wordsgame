package app.wordgame.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    const val APP_ID = "ca-app-pub-2498267529185476~7471990700"
    const val BANNER_LANGUAGE_ID = "ca-app-pub-9651830078758870/5768416748"
    const val BANNER_GAME_ID = "ca-app-pub-9651830078758870/6658511952"
    const val APP_OPEN_ID = "ca-app-pub-9651830078758870/6015828752"
    const val INTERSTITIAL_ID = "ca-app-pub-9651830078758870/5345430289"
    const val REWARDED_EXTRA_TRY_ID = "ca-app-pub-9651830078758870/5301512885"
    const val REWARDED_SOLUTION_ID = "ca-app-pub-9651830078758870/5237619075"

    const val INTERSTITIAL_REWARD_FALLBACK_ID = "ca-app-pub-9651830078758870/8789851277"

    // IDs de test Google officiels
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var isTestMode = false

    private var interstitialAd: InterstitialAd? = null
    private var interstitialRewardFallback: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var rewardedAdExtraTry: RewardedAd? = null
    private var rewardedAdSolution: RewardedAd? = null

    private var isLoadingInterstitial = false
    private var isLoadingInterstitialFallback = false
    private var isLoadingAppOpen = false
    private var isLoadingRewardedExtraTry = false
    private var isLoadingRewardedSolution = false

    private var lastInterstitialTime = 0L
    private var lastAdClickTime = 0L
    private const val INTERSTITIAL_INTERVAL = 5 * 60 * 1000L
    // Délai minimum entre deux clics sur une annonce (protection anti-clic invalide)
    private const val AD_CLICK_COOLDOWN_MS = 30_000L

    // ───────────────────────────────────────────────────────────────────────────
    // IMPORTANT — Protection appareil physique de développeur :
    // Ajoutez le hash MD5 de votre appareil pour qu'il reçoive toujours des
    // annonces de test (même en release). Pour obtenir le hash :
    //   1. Lancez l'app en debug sur votre appareil
    //   2. Cherchez dans Logcat : "Use RequestConfiguration.Builder().setTestDeviceIds"
    //   3. Copiez le hash et collez-le ci-dessous
    // ───────────────────────────────────────────────────────────────────────────
    private val DEVELOPER_DEVICE_IDS = listOf<String>(
        // Ajoutez ici le hash de votre appareil, ex :
        // "ABCDEF1234567890ABCDEF1234567890"
    )

    fun initialize(context: Context, debugMode: Boolean = false) {
        isTestMode = debugMode
        if (isTestMode) {
            Log.w(TAG, "⚠️ AdMob en MODE TEST — aucune annonce réelle ne sera servie")
        }

        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap}")
        }

        // L'émulateur + les appareils du développeur reçoivent toujours des annonces de test
        val testDeviceIds = mutableListOf(AdRequest.DEVICE_ID_EMULATOR)
        testDeviceIds.addAll(DEVELOPER_DEVICE_IDS)
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)
    }

    fun recordAdClick() {
        lastAdClickTime = System.currentTimeMillis()
    }

    private fun isAdClickAllowed(): Boolean {
        val elapsed = System.currentTimeMillis() - lastAdClickTime
        return elapsed > AD_CLICK_COOLDOWN_MS
    }

    fun setTestMode(enabled: Boolean) {
        isTestMode = enabled
    }

    fun getBannerAdId(isLanguageScreen: Boolean): String {
        return if (isTestMode) TEST_BANNER_ID
        else if (isLanguageScreen) BANNER_LANGUAGE_ID else BANNER_GAME_ID
    }

    fun getInterstitialAdId(): String =
        if (isTestMode) TEST_INTERSTITIAL_ID else INTERSTITIAL_ID

    fun getInterstitialRewardFallbackId(): String =        // ✅
        if (isTestMode) TEST_INTERSTITIAL_ID else INTERSTITIAL_REWARD_FALLBACK_ID

    fun getAppOpenAdId(): String =
        if (isTestMode) TEST_APP_OPEN_ID else APP_OPEN_ID

    fun getRewardedExtraTryId(): String =
        if (isTestMode) TEST_REWARDED_ID else REWARDED_EXTRA_TRY_ID

    fun getRewardedSolutionId(): String =
        if (isTestMode) TEST_REWARDED_ID else REWARDED_SOLUTION_ID

    fun createBannerAd(context: Context, isLanguageScreen: Boolean): AdView {
        return AdView(context).apply {
            adUnitId = getBannerAdId(isLanguageScreen)
            setAdSize(AdSize.BANNER)
            loadAd(AdRequest.Builder().build())
        }
    }

    fun loadInterstitial(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true

        InterstitialAd.load(
            context,
            getInterstitialAdId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "✅ Interstitial loaded")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    onAdLoaded()

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitial(context)
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Failed to show interstitial: ${error.message}")
                            interstitialAd = null
                        }
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial shown")
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load interstitial: ${error.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    // À appeler uniquement sur action utilisateur (fin de partie, navigation entre écrans).
    // NE JAMAIS appeler depuis un timer automatique.
    fun showInterstitialIfReady(activity: Activity): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastInterstitialTime < INTERSTITIAL_INTERVAL) {
            Log.d(TAG, "Interstitial interval not met yet")
            return false
        }

        if (!isAdClickAllowed()) {
            Log.d(TAG, "Ad click cooldown active — interstitial skipped")
            return false
        }

        return if (interstitialAd != null) {
            interstitialAd?.show(activity)
            lastInterstitialTime = currentTime
            true
        } else {
            Log.d(TAG, "Interstitial not ready")
            loadInterstitial(activity)
            false
        }
    }
    fun loadInterstitialRewardFallback(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingInterstitialFallback) return
        isLoadingInterstitialFallback = true

        InterstitialAd.load(
            context,
            getInterstitialRewardFallbackId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "✅ Interstitiel FALLBACK ligne 6 chargé")
                    interstitialRewardFallback = ad
                    isLoadingInterstitialFallback = false
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Échec Interstitiel FALLBACK: ${error.message}")
                    interstitialRewardFallback = null
                    isLoadingInterstitialFallback = false
                }
            }
        )
    }

    private fun showInterstitialRewardFallback(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val ad = interstitialRewardFallback

        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "✅ Interstitiel FALLBACK fermé → récompense accordée")
                    interstitialRewardFallback = null
                    // Récompense toujours donnée (même logique que Rewarded)
                    onRewarded()
                    onAdDismissed()
                    // Recharger pour la prochaine utilisation
                    loadInterstitialRewardFallback(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "❌ Échec affichage Interstitiel FALLBACK: ${error.message}")
                    interstitialRewardFallback = null
                    // Pas de récompense si l'annonce n'a pas été vue (règles Google)
                    onAdDismissed()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "✅ Interstitiel FALLBACK affiché")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "⏳ Interstitiel FALLBACK pas disponible")
            // Aucune annonce disponible du tout → appeler onAdDismissed sans récompense
            onAdDismissed()
        }
    }

    fun loadAppOpenAd(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingAppOpen || appOpenAd != null) return
        isLoadingAppOpen = true

        AppOpenAd.load(
            context,
            getAppOpenAdId(),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "✅ App open ad loaded")
                    appOpenAd = ad
                    isLoadingAppOpen = false
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load app open ad: ${error.message}")
                    appOpenAd = null
                    isLoadingAppOpen = false
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (appOpenAd != null) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    onAdDismissed()
                    loadAppOpenAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    onAdDismissed()
                }
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App open ad shown")
                }
            }
            appOpenAd?.show(activity)
        } else {
            onAdDismissed()
            loadAppOpenAd(activity)
        }
    }

    fun loadRewardedAdExtraTry(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingRewardedExtraTry || rewardedAdExtraTry != null) return
        isLoadingRewardedExtraTry = true

        RewardedAd.load(
            context,
            getRewardedExtraTryId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded EXTRA TRY loaded")
                    rewardedAdExtraTry = ad
                    isLoadingRewardedExtraTry = false
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load Rewarded EXTRA TRY: ${error.message}")
                    rewardedAdExtraTry = null
                    isLoadingRewardedExtraTry = false
                    loadInterstitialRewardFallback(context)
                }
            }
        )
    }

    fun showRewardedAdExtraTry(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        when {
            // Priorité 1 : Rewarded Ad
            rewardedAdExtraTry != null -> {
                Log.d(TAG, "📺 Affichage Rewarded EXTRA TRY")
                rewardedAdExtraTry?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "✅ Rewarded EXTRA TRY fermée")
                        rewardedAdExtraTry = null
                        onAdDismissed()
                        loadRewardedAdExtraTry(activity)
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        Log.e(TAG, "❌ Échec affichage Rewarded EXTRA TRY: ${error.message}")
                        rewardedAdExtraTry = null
                        onAdDismissed()
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "✅ Rewarded EXTRA TRY affichée")
                    }
                }
                rewardedAdExtraTry?.show(activity) { rewardItem ->
                    Log.d(TAG, "🎁 Récompense EXTRA TRY: ${rewardItem.amount} ${rewardItem.type}")
                    onRewarded()
                }
            }

            interstitialRewardFallback != null -> {
                Log.d(TAG, "📺 Rewarded non dispo → Interstitiel FALLBACK pour ligne 6")
                showInterstitialRewardFallback(activity, onRewarded, onAdDismissed)
            }

            else -> {
                Log.d(TAG, "⏳ Aucune annonce EXTRA TRY disponible")
                onAdDismissed()
            }
        }
    }

    fun loadRewardedAdSolution(context: Context, onAdLoaded: () -> Unit = {}) {
        if (isLoadingRewardedSolution || rewardedAdSolution != null) return
        isLoadingRewardedSolution = true

        RewardedAd.load(
            context,
            getRewardedSolutionId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded SOLUTION loaded")
                    rewardedAdSolution = ad
                    isLoadingRewardedSolution = false
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load Rewarded SOLUTION: ${error.message}")
                    rewardedAdSolution = null
                    isLoadingRewardedSolution = false
                    loadInterstitialRewardFallback(context)
                }
            }
        )
    }

    fun showRewardedAdSolution(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        when {
            // Priorité 1 : Rewarded Ad
            rewardedAdSolution != null -> {
                Log.d(TAG, "📺 Affichage Rewarded SOLUTION")
                rewardedAdSolution?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "✅ Rewarded SOLUTION fermée")
                        rewardedAdSolution = null
                        onAdDismissed()
                        loadRewardedAdSolution(activity)
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        Log.e(TAG, "❌ Échec affichage Rewarded SOLUTION: ${error.message}")
                        rewardedAdSolution = null
                        onAdDismissed()
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "✅ Rewarded SOLUTION affichée")
                    }
                }
                rewardedAdSolution?.show(activity) { rewardItem ->
                    Log.d(TAG, "🎁 Récompense SOLUTION: ${rewardItem.amount} ${rewardItem.type}")
                    onRewarded()
                }
            }

            interstitialRewardFallback != null -> {
                Log.d(TAG, "📺 Rewarded non dispo → Interstitiel FALLBACK pour Solution")
                showInterstitialRewardFallback(activity, onRewarded, onAdDismissed)
            }

            else -> {
                Log.d(TAG, "⏳ Aucune annonce SOLUTION disponible")
                onAdDismissed()
            }
        }
    }

    fun isRewardedAdExtraTryAvailable(): Boolean {
        return rewardedAdExtraTry != null || interstitialRewardFallback != null
    }

    fun isRewardedAdSolutionAvailable(): Boolean {
        return rewardedAdSolution != null || interstitialRewardFallback != null
    }
}