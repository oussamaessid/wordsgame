package app.wordgame.presentation.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DailyWordGameScreen(
    language: app.wordgame.domain.model.Language,
    onBackToLanguageSelection: () -> Unit
) {
    val viewModel: app.wordgame.presentation.viewmodel.GameViewModel = viewModel(
        factory = _root_ide_package_.app.wordgame.di.AppContainer.provideViewModelFactory(),
        key = "game_vm_${language.name}"
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialisation du jeu au changement de langue
    LaunchedEffect(language) {
        viewModel.initializeGame(language)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1976D2))
        }
        return
    }

    if (uiState.gameOver && !uiState.showRewardedAdDialog) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
        ) {
            StatsDialog(
                won = uiState.won,
                gameOver = uiState.gameOver,
                attempts = uiState.guesses.size,
                targetWord = uiState.targetWord,
                stats = uiState.stats,
                onDismiss = onBackToLanguageSelection,
                language = language,
                gameStartTime = uiState.gameStartTime,
                gameEndTime = uiState.gameEndTime
            )
        }
        return
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val isSmallScreen = screenHeight < 700

    val cellSize = when {
        screenHeight < 600 -> 38.dp
        screenHeight < 700 -> 45.dp
        screenHeight < 800 -> 52.dp
        else -> 56.dp
    }
    val cellSpacing = when {
        screenHeight < 600 -> 4.dp
        screenHeight < 700 -> 6.dp
        else -> 8.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .systemBarsPadding()
        ) {
            // Header
            GameHeader(
                title = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "MOT DU JOUR" else "DAILY WORD",
                subtitle = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "Aujourd'hui" else "Today",
                backButtonText = if (language == _root_ide_package_.app.wordgame.domain.model.Language.FRENCH) "Retour" else "Back",
                onBackClick = onBackToLanguageSelection,
                onStatsClick = { viewModel.toggleStatsDialog(true) },
                isSmallScreen = isSmallScreen,
                language = language
            )

            // Grille centrée
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Calculer le nombre maximum de lignes à afficher
                // Normalement 5, mais si on a 5 guesses déjà et pas de dialogue = on peut en avoir 6
                val maxAttempts = when {
                    uiState.showRewardedAdDialog -> 5  // Dialogue affiché = 5 lignes
                    uiState.guesses.size >= 5 -> 6     // 5+ tentatives = afficher 6 lignes
                    else -> 5                           // Sinon = 5 lignes
                }

                GameGrid(
                    currentGuess = uiState.currentGuess,
                    guesses = uiState.guesses,
                    viewModel = viewModel,
                    cellSize = cellSize,
                    cellSpacing = cellSpacing,
                    maxAttempts = maxAttempts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clavier
            GameKeyboard(
                onKeyPress = { key -> viewModel.onKeyPressed(key) },
                viewModel = viewModel,
                gameOver = uiState.gameOver,
                language = language,
                currentGuessLength = uiState.currentGuess.length
            )

            // Bannière publicitaire
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(bottom = 8.dp)
            ) {
                BannerAdView(
                    isLanguageScreen = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stats accessibles manuellement pendant la partie
            if (uiState.showStats) {
                Box(modifier = Modifier.fillMaxSize()) {
                    StatsDialog(
                        won = uiState.won,
                        gameOver = false,
                        attempts = uiState.guesses.size,
                        targetWord = uiState.targetWord,
                        stats = uiState.stats,
                        onDismiss = { viewModel.toggleStatsDialog(false) },
                        language = language,
                        gameStartTime = uiState.gameStartTime,
                        gameEndTime = uiState.gameEndTime
                    )
                }
            }
        }

        if (uiState.showRewardedAdDialog) {
            RewardedAdChoiceDialog(
                onWatchExtraTryAd = {
                    viewModel.hideRewardedAdDialog()

                    app.wordgame.ads.AdManager.showRewardedAdExtraTry(
                        activity = context as Activity,
                        onRewarded = {
                            viewModel.addExtraTry()
                        },
                        onAdDismissed = {
                            // Vidéo fermée sans la regarder
                        }
                    )
                },
                onDismiss = {
                    // Cancel → Perdu
                    viewModel.finishGameAsLost()
                },
                language = language
            )
        }
    }
}