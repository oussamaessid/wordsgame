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
    const val BANNER_LANGUAGE_ID = "ca-app-pub-2498267529185476/6158909031"
    const val BANNER_GAME_ID = "ca-app-pub-2498267529185476/3888076711"
    const val APP_OPEN_ID = "ca-app-pub-2498267529185476/1261913379"
    const val INTERSTITIAL_ID = "ca-app-pub-2498267529185476/2260026625"
    const val REWARDED_EXTRA_TRY_ID = "ca-app-pub-2498267529185476/2881272915"
    const val REWARDED_SOLUTION_ID = "ca-app-pub-2498267529185476/7307603089"

    const val INTERSTITIAL_REWARD_FALLBACK_ID = "ca-app-pub-2498267529185476/2506921717"

    // IDs de test
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Mode test (changez à false pour production)
    private var isTestMode = false

    private var interstitialAd: InterstitialAd? = null
    private var interstitialRewardFallback: InterstitialAd? = null  // ✅ Fallback pour ligne 6
    private var appOpenAd: AppOpenAd? = null
    private var rewardedAdExtraTry: RewardedAd? = null
    private var rewardedAdSolution: RewardedAd? = null

    private var isLoadingInterstitialFallback = false  // ✅ Éviter double chargement

    private var lastInterstitialTime = 0L
    private const val INTERSTITIAL_INTERVAL = 5 * 60 * 1000L // 5 minutes

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap}")
        }

        if (isTestMode) {
            val testDeviceIds = listOf(AdRequest.DEVICE_ID_EMULATOR)
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
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
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            getInterstitialAdId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "✅ Interstitial loaded")
                    interstitialAd = ad
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
                }
            }
        )
    }

    fun showInterstitialIfReady(activity: Activity): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastInterstitialTime < INTERSTITIAL_INTERVAL) {
            Log.d(TAG, "Interstitial interval not met yet")
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
                    // En cas d'échec d'affichage, on donne quand même la récompense
                    onRewarded()
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
        AppOpenAd.load(
            context,
            getAppOpenAdId(),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "✅ App open ad loaded")
                    appOpenAd = ad
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load app open ad: ${error.message}")
                    appOpenAd = null
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
        RewardedAd.load(
            context,
            getRewardedExtraTryId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded EXTRA TRY loaded")
                    rewardedAdExtraTry = ad
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load Rewarded EXTRA TRY: ${error.message}")
                    rewardedAdExtraTry = null
                    // ✅ Charger le fallback automatiquement si Rewarded échoue
                    Log.d(TAG, "🔄 Chargement Interstitiel FALLBACK en remplacement...")
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
        RewardedAd.load(
            context,
            getRewardedSolutionId(),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded SOLUTION loaded")
                    rewardedAdSolution = ad
                    onAdLoaded()
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load Rewarded SOLUTION: ${error.message}")
                    rewardedAdSolution = null
                    // ✅ Charger le fallback automatiquement si Rewarded échoue
                    Log.d(TAG, "🔄 Chargement Interstitiel FALLBACK en remplacement...")
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