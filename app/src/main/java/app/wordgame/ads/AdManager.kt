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

    // IDs de production
    const val APP_ID = "ca-app-pub-2498267529185476~7471990700"
    const val BANNER_LANGUAGE_ID = "ca-app-pub-2498267529185476/6158909031"
    const val BANNER_GAME_ID = "ca-app-pub-2498267529185476/3888076711"
    const val APP_OPEN_ID = "ca-app-pub-2498267529185476/1261913379"
    const val INTERSTITIAL_ID = "ca-app-pub-2498267529185476/2260026625"
    const val REWARDED_EXTRA_TRY_ID = "ca-app-pub-2498267529185476/2881272915"
    const val REWARDED_SOLUTION_ID = "ca-app-pub-2498267529185476/7307603089"

    // IDs de test
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Mode test (changez à false pour production)
    private var isTestMode = false

    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var rewardedAdExtraTry: RewardedAd? = null
    private var rewardedAdSolution: RewardedAd? = null
    private var lastInterstitialTime = 0L
    private const val INTERSTITIAL_INTERVAL = 5 * 60 * 1000L // 5 minutes

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: ${initStatus.adapterStatusMap}")
        }

        // Configuration pour les tests
        if (isTestMode) {
            val testDeviceIds = listOf(
                AdRequest.DEVICE_ID_EMULATOR,
            )
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
        return if (isTestMode) {
            TEST_BANNER_ID
        } else {
            if (isLanguageScreen) BANNER_LANGUAGE_ID else BANNER_GAME_ID
        }
    }

    fun getInterstitialAdId(): String {
        return if (isTestMode) TEST_INTERSTITIAL_ID else INTERSTITIAL_ID
    }

    fun getAppOpenAdId(): String {
        return if (isTestMode) TEST_APP_OPEN_ID else APP_OPEN_ID
    }

    fun getRewardedExtraTryId(): String {
        return if (isTestMode) TEST_REWARDED_ID else REWARDED_EXTRA_TRY_ID
    }

    fun getRewardedSolutionId(): String {
        return if (isTestMode) TEST_REWARDED_ID else REWARDED_SOLUTION_ID
    }

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
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    onAdLoaded()

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            loadInterstitial(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Failed to show interstitial: ${error.message}")
                            interstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad shown")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial: ${error.message}")
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

    fun loadAppOpenAd(context: Context, onAdLoaded: () -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            getAppOpenAdId(),
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded")
                    appOpenAd = ad
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load app open ad: ${error.message}")
                    appOpenAd = null
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (appOpenAd != null) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed")
                    appOpenAd = null
                    onAdDismissed()
                    loadAppOpenAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Failed to show app open ad: ${error.message}")
                    appOpenAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App open ad shown")
                }
            }

            appOpenAd?.show(activity)
        } else {
            Log.d(TAG, "App open ad not ready")
            onAdDismissed()
            loadAppOpenAd(activity)
        }
    }

    fun loadRewardedAdExtraTry(context: Context, onAdLoaded: () -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            getRewardedExtraTryId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded ad EXTRA TRY loaded")
                    rewardedAdExtraTry = ad
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load rewarded EXTRA TRY: ${error.message}")
                    rewardedAdExtraTry = null
                }
            }
        )
    }

    /**
     * Charger la vidéo avec récompense pour révéler la solution
     */
    fun loadRewardedAdSolution(context: Context, onAdLoaded: () -> Unit = {}) {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            getRewardedSolutionId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "✅ Rewarded ad SOLUTION loaded")
                    rewardedAdSolution = ad
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "❌ Failed to load rewarded SOLUTION: ${error.message}")
                    rewardedAdSolution = null
                }
            }
        )
    }

    /**
     * Afficher la vidéo avec récompense pour essai supplémentaire
     */
    fun showRewardedAdExtraTry(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (rewardedAdExtraTry != null) {
            rewardedAdExtraTry?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "✅ Rewarded EXTRA TRY dismissed")
                    rewardedAdExtraTry = null
                    onAdDismissed()
                    loadRewardedAdExtraTry(activity)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "❌ Failed to show rewarded EXTRA TRY: ${error.message}")
                    rewardedAdExtraTry = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "✅ Rewarded EXTRA TRY shown")
                }
            }

            rewardedAdExtraTry?.show(activity) { rewardItem ->
                Log.d(TAG, "🎁 User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            Log.d(TAG, "⏳ Rewarded EXTRA TRY not ready")
            onAdDismissed()
        }
    }

    /**
     * Afficher la vidéo avec récompense pour révéler la solution
     */
    fun showRewardedAdSolution(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (rewardedAdSolution != null) {
            rewardedAdSolution?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "✅ Rewarded SOLUTION dismissed")
                    rewardedAdSolution = null
                    onAdDismissed()
                    loadRewardedAdSolution(activity)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "❌ Failed to show rewarded SOLUTION: ${error.message}")
                    rewardedAdSolution = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "✅ Rewarded SOLUTION shown")
                }
            }

            rewardedAdSolution?.show(activity) { rewardItem ->
                Log.d(TAG, "🎁 User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            Log.d(TAG, "⏳ Rewarded SOLUTION not ready")
            onAdDismissed()
        }
    }

    /**
     * Vérifier si la vidéo EXTRA TRY est disponible
     */
    fun isRewardedAdExtraTryAvailable(): Boolean {
        return rewardedAdExtraTry != null
    }

    /**
     * Vérifier si la vidéo SOLUTION est disponible
     */
    fun isRewardedAdSolutionAvailable(): Boolean {
        return rewardedAdSolution != null
    }
}